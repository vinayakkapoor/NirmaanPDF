package com.pdf.nirmaanpdf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PDFActivity extends AppCompatActivity {

    private FloatingActionButton uploadButton;
    private Intent filePicker;
    private String projectName;
    private String filePath;
    private String fileName;
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    private ListView pdfListView;
    List<uploadHelper> pdfList;
    boolean longPress=false;
    List<String> keys;
    private TextView retrieving;
    ArrayAdapter<String> ad;
    boolean download=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_d_f);

        projectName=getIntent().getStringExtra("project");
        setTitle(projectName);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("uploads/"+projectName);
        mStorageReference = FirebaseStorage.getInstance().getReference();

        uploadButton = (FloatingActionButton)findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePicker = new Intent(Intent.ACTION_GET_CONTENT);
                filePicker.setType("application/pdf");
                filePicker.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(filePicker,500);
            }
        });

        retrieving=(TextView)findViewById(R.id.retrieving);
        retrieving.setVisibility(View.VISIBLE);
        pdfListView = (ListView)findViewById(R.id.listView);
        pdfList = new ArrayList<>();
        viewFiles();
        pdfListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                uploadHelper uploadHelp = pdfList.get(position);

                WebView webView = (WebView)findViewById(R.id.webview);
                String url = (String.valueOf(Uri.parse(uploadHelp.getFileUrl())));

                webView.setWebViewClient(new WebViewClient());

                webView.loadUrl(url);

                //handle downloading
                webView.setDownloadListener(new DownloadListener()
                {
                    @Override
                    public void onDownloadStart(String url, String userAgent,
                                                String contentDisposition, String mimeType,
                                                long contentLength) {
                        if(download) {

                            DownloadManager.Request request = new DownloadManager.Request(
                                    Uri.parse(url));
                            request.setMimeType(mimeType);
                            String cookies = CookieManager.getInstance().getCookie(url);
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                        url, contentDisposition, mimeType));

                            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                            dm.enqueue(request);
                            Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            download=true;
                        }
                    }});

            }
        });

        pdfListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(PDFActivity.this);

                builder.setMessage("Delete this file?").setTitle("Alert");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @SuppressLint("RestrictedApi")
                    public void onClick(DialogInterface dialog, int id) {
                      FirebaseDatabase.getInstance().getReference("uploads/"+projectName).child(keys.get(position)).setValue(null);
                      StorageReference reference = mStorageReference.child("uploads/"+projectName+"/"+pdfList.get(position).getFileName());
                      reference.delete();
                      Toast.makeText(PDFActivity.this, pdfList.get(position).getFileName()+" deleted!", Toast.LENGTH_SHORT).show();
                      download=false;
                      pdfList.clear();
                      ad.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                longPress=true;
                AlertDialog dialog = builder.create();
                dialog.show();
               return false;
            }
        });
    }

    private void viewFiles() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!pdfList.isEmpty()){
                    pdfList.clear();
                    ad.notifyDataSetChanged();
                }
                keys = new ArrayList<String>();
                int x=0;
                for(DataSnapshot postSnapshot:snapshot.getChildren()){
                    keys.add(postSnapshot.getKey());
                    pdfList.add(postSnapshot.getValue(uploadHelper.class));
                }
                int s=pdfList.size();
                String[] uploads = new String[s];
                for (int i=0; i < s;i++){
                    uploads[i] = pdfList.get(i).getFileName();
                }

                ad = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,uploads);
                retrieving.setVisibility(View.INVISIBLE);
                pdfListView.setAdapter(ad);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500){ //&& resultCode == RESULT_OK) {
            try {

                assert data != null;
                filePath = Objects.requireNonNull(data.getData()).getPath();
                getFileName(data.getData());
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    private void uploadPDFFile(Uri data) {

        final ProgressDialog progressDialog= new ProgressDialog(this);

        progressDialog.setTitle("Uploading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        StorageReference reference = mStorageReference.child("uploads/"+projectName+"/"+fileName);
        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uri= taskSnapshot.getStorage().getDownloadUrl();
                        while(!uri.isComplete());
                        Uri url=uri.getResult();

                        assert url != null;
                        uploadHelper uploadPdf = new uploadHelper(fileName,url.toString());
                        mDatabaseReference.child(mDatabaseReference.push().getKey()).setValue(uploadPdf);
                        Toast.makeText(PDFActivity.this, "File uploaded!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        pdfList.clear();
                        ad.notifyDataSetChanged();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded: "+(int)progress+"%");
            }
        });
    }

    private void getFileName(final Uri data)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Upload PDF");
        alert.setMessage("Enter name of file(DON'T enter '.pdf' extension)");

        final EditText input = new EditText(this);
        //input.setMaxWidth(5);
        input.setWidth(5);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                fileName = input.getText().toString().trim()+".pdf";
                uploadPDFFile(data);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(PDFActivity.this, "Uploading Cancelled", Toast.LENGTH_SHORT).show();
                return;
            }
        });
        alert.setCancelable(false);
        alert.show();
    }
}