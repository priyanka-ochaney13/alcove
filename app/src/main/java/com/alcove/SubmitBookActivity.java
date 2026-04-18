package com.alcove;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alcove.api.ApiClient;
import com.alcove.models.BookCreate;
import com.alcove.models.BookResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitBookActivity extends AppCompatActivity {

    private EditText titleEdit, authorsEdit, descriptionEdit,
                     pagesEdit, publishedDateEdit, imageUrlEdit, genresEdit;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_submit_book);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        titleEdit         = findViewById(R.id.titleEdit);
        authorsEdit       = findViewById(R.id.authorsEdit);
        descriptionEdit   = findViewById(R.id.descriptionEdit);
        pagesEdit         = findViewById(R.id.pagesEdit);
        publishedDateEdit = findViewById(R.id.publishedDateEdit);
        imageUrlEdit      = findViewById(R.id.imageUrlEdit);
        genresEdit        = findViewById(R.id.genresEdit);
        submitBtn         = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(v -> submitBook());
    }

    private void submitBook() {
        String title   = titleEdit.getText().toString().trim();
        String authors = authorsEdit.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (authors.isEmpty()) {
            Toast.makeText(this, "At least one author is required", Toast.LENGTH_SHORT).show();
            return;
        }

        BookCreate request = new BookCreate();
        request.setTitle(title);
        request.setAuthors(authors);
        request.setDescription(descriptionEdit.getText().toString().trim());
        request.setPublishedDate(publishedDateEdit.getText().toString().trim());
        request.setImageUrl(imageUrlEdit.getText().toString().trim());
        request.setCategories(genresEdit.getText().toString().trim());

        String pagesStr = pagesEdit.getText().toString().trim();
        if (!pagesStr.isEmpty()) {
            try { request.setPageCount(Integer.parseInt(pagesStr)); }
            catch (NumberFormatException ignored) {}
        }

        submitBtn.setEnabled(false);
        ApiClient.getService().submitBook(request).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                submitBtn.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SubmitBookActivity.this,
                        "\"" + response.body().getTitle() + "\" added successfully!",
                        Toast.LENGTH_LONG).show();
                    finish();
                } else if (response.code() == 400) {
                    Toast.makeText(SubmitBookActivity.this,
                        "A book with this title already exists",
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SubmitBookActivity.this,
                        "Failed to submit book", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                submitBtn.setEnabled(true);
                Toast.makeText(SubmitBookActivity.this,
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

