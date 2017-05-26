/*
 * Open source project DoorPhone
 * @author Andreas Pfister
 * Copyright (C) 2017
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.d0pam1n.doorphone.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.d0pam1n.doorphone.R;

public class AboutActivity extends BaseActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_about, contentFrameLayout);

        createLinks();
        setVersionNumber();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMenuItem(R.id.nav_drawer_item_about);
    }

    private void createLinks() {


        TextView linkRepo = (TextView) findViewById(R.id.about_repository);
        linkRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/d0pam1n/doorphone"));
                startActivity(i);

            }
        });

        TextView reportBug = (TextView) findViewById(R.id.about_report_bug);
        reportBug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/d0pam1n/doorphone/issues"));
                startActivity(i);
            }
        });

        TextView mailAuthor = (TextView) findViewById(R.id.about_author_mail);
        mailAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailintent = new Intent(android.content.Intent.ACTION_SEND);
                emailintent.setType("plain/text");
                emailintent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] {getString(R.string.about_author_mail) });
                emailintent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                emailintent.putExtra(android.content.Intent.EXTRA_TEXT,"");
                startActivity(Intent.createChooser(emailintent, "Send mail..."));
            }
        });
    }

    private void setVersionNumber() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;

            TextView versionView = (TextView) findViewById(R.id.about_project_version);
            versionView.setText("v" + version);
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, getString(R.string.about_error_version_not_found));
        }

    }
}
