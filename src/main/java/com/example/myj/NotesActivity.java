package com.example.myj;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.AccessControlContext;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private EditText titleEditText, contentEditText;
    private ImageButton saveNoteBtn, addImageButton;
    private TextView pageTitleTextView, deleteNoteTextViewBtn;
    private ImageView imageView;
    private String title, content, docId;
    private boolean isEditMode = false;
    private Uri imageUri; // Uri to store the selected image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn);
        addImageButton = findViewById(R.id.add_image_btn);
        imageView = findViewById(R.id.note_image_view);

        // receive data
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        if (docId != null && !docId.isEmpty()) {
            isEditMode = true;
        }

        titleEditText.setText(title);
        contentEditText.setText(content);
        if (isEditMode) {
            pageTitleTextView.setText("Edit your note");
            deleteNoteTextViewBtn.setVisibility(View.VISIBLE);
        }

        saveNoteBtn.setOnClickListener(v -> saveNote());
        addImageButton.setOnClickListener(v -> chooseImage());
        deleteNoteTextViewBtn.setOnClickListener(v -> deleteNoteFromFirebase());
    }

    private void saveNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        if (noteTitle == null || noteTitle.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }
        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());

        // Save image if available
        if (imageUri != null) {
            saveImageToFirebase(imageUri, note);
        } else {
            saveNoteToFirebase(note);
        }
    }

    private void saveNoteToFirebase(Note note) {
        DocumentReference documentReference;
        if (isEditMode) {
            // update the note
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        } else {
            // create new note
            documentReference = Utility.getCollectionReferenceForNotes().document();
        }

        documentReference.set(note).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // note is added
                Utility.showToast(NotesActivity.this, "Note added successfully");
                finish();
            } else {
                Utility.showToast(NotesActivity.this, "Failed while adding note");
            }
        });
    }

    private void saveImageToFirebase(Uri imageUri, Note note) {
        // Upload image to Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + Objects.requireNonNull(imageUri.getLastPathSegment()));
        storageRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the download URL of the uploaded image
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    note.setImageUrl(imageUrl);
                    saveNoteToFirebase(note); // Save note with image URL
                }).addOnFailureListener(e -> {
                    Utility.showToast(NotesActivity.this, "Failed to get image URL");
                    Log.e("NotesActivity", "Failed to get image URL", e); // Log the exception
                });
            } else {
                Utility.showToast(NotesActivity.this, "Failed to upload image");
                Log.e("NotesActivity", "Failed to upload image", task.getException()); // Log the exception
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void deleteNoteFromFirebase() {
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        documentReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // note is deleted
                Utility.showToast(NotesActivity.this, "Note deleted successfully");
                finish();
            } else {
                Utility.showToast(NotesActivity.this, "Failed while deleting note");
            }
        });
    }
}
