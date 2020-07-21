package com.pdf.nirmaanpdf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView sko;
    private TextView pkp;
    private TextView baas;
    private TextView sap;
    private TextView pcd;
    private TextView utk;
    private TextView un1;
    private TextView un2;
    private TextView you;
    private TextView dis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sko = (TextView)findViewById(R.id.sko);
        pkp = (TextView)findViewById(R.id.pkp);
        baas = (TextView)findViewById(R.id.baas);
        sap = (TextView)findViewById(R.id.sap);
        pcd = (TextView)findViewById(R.id.pcd);
        utk = (TextView)findViewById(R.id.utk);
        un1 = (TextView)findViewById(R.id.un1);
        un2 = (TextView)findViewById(R.id.un2);
        you = (TextView)findViewById(R.id.you);
        dis = (TextView)findViewById(R.id.dis);

        View.OnClickListener on_Click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pdfIntent = new Intent(MainActivity.this,PDFActivity.class);
                pdfIntent.putExtra("project",(v.getResources().getResourceName(v.getId())).substring(
                        v.getResources().getResourceName(v.getId()).lastIndexOf('/')+1
                ));
                startActivity(pdfIntent);
            }
        };
        sko.setOnClickListener(on_Click);
        pkp.setOnClickListener(on_Click);
        baas.setOnClickListener(on_Click);
        sap.setOnClickListener(on_Click);
        pcd.setOnClickListener(on_Click);
        utk.setOnClickListener(on_Click);
        un1.setOnClickListener(on_Click);
        un2.setOnClickListener(on_Click);
        you.setOnClickListener(on_Click);
        dis.setOnClickListener(on_Click);
    }
}
