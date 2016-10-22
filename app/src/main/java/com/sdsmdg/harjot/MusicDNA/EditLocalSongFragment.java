package com.sdsmdg.harjot.MusicDNA;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditLocalSongFragment extends Fragment {

    EditText titleText, artistText, albumText;
    ImageView songImage;
    Button saveButton;

    Context ctx;

    boolean isTitleNotNull = false;
    boolean isArtistNotNull = false;
    boolean isAlbumNotNull = false;

    MP3File mp3File;

    onEditSongSaveListener mCallback;

    public interface onEditSongSaveListener {
        public void onEditSongSave(boolean wasSaveSuccessful);
    }

    public EditLocalSongFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = context;
        try {
            mCallback = (onEditSongSaveListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_local_song, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleText = (EditText) view.findViewById(R.id.edit_song_title);
//        titleText.setText(HomeActivity.editSong.getTitle());
        artistText = (EditText) view.findViewById(R.id.edit_song_artist);
//        artistText.setText(HomeActivity.editSong.getArtist());
        albumText = (EditText) view.findViewById(R.id.edit_song_album);
//        albumText.setText(HomeActivity.editSong.getAlbum());

        songImage = (ImageView) view.findViewById(R.id.edit_song_image);

        Bitmap bmp = null;
        try {
            bmp = getBitmap(HomeActivity.editSong.getPath());
        } catch (Exception e) {

        }

        if (bmp != null) {
            songImage.setImageBitmap(bmp);
        } else {
            songImage.setImageResource(R.drawable.ic_default);
        }

        saveButton = (Button) view.findViewById(R.id.edit_song_save_button);
        saveButton.setBackgroundColor(HomeActivity.themeColor);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!titleText.getText().toString().trim().equals("")) {
                    HomeActivity.editSong.setTitle(titleText.getText().toString().trim());
                    isTitleNotNull = true;
                } else {
                    titleText.setError("Enter a valid Title");
                    isTitleNotNull = false;
                }
                if (!artistText.getText().toString().trim().equals("")) {
                    HomeActivity.editSong.setTitle(artistText.getText().toString().trim());
                    isArtistNotNull = true;
                } else {
                    artistText.setError("Enter a valid Artist name");
                    isArtistNotNull = false;
                }
                if (!albumText.getText().toString().trim().equals("")) {
                    HomeActivity.editSong.setTitle(albumText.getText().toString().trim());
                    isAlbumNotNull = true;
                } else {
                    albumText.setError("Enter a valid Album name");
                    isAlbumNotNull = false;
                }
                if (isTitleNotNull && isArtistNotNull && isAlbumNotNull) {

                    ProgressDialog progressDialog = new ProgressDialog(ctx);

                    Tag tag = mp3File.getTag();

                    progressDialog.setMessage("Saving");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(true);

//                    id3v2Tag.setTitle(titleText.getText().toString());
//                    id3v2Tag.setArtist(artistText.getText().toString());
//                    id3v2Tag.setAlbum(albumText.getText().toString());

                    boolean error = false;

                    try {
                        tag.setField(FieldKey.TITLE, titleText.getText().toString());
                        tag.setField(FieldKey.ARTIST, artistText.getText().toString());
                        tag.setField(FieldKey.ALBUM, albumText.getText().toString());
                    } catch (FieldDataInvalidException e) {
                        error = true;
                        e.printStackTrace();
                    }

                    try {
                        mp3File.commit();
                    } catch (CannotWriteException e) {
                        error = true;
                        e.printStackTrace();
                    }

                    if (!error) {
                        HomeActivity.editSong.setTitle(titleText.getText().toString());
                        HomeActivity.editSong.setArtist(artistText.getText().toString());
                        HomeActivity.editSong.setAlbum(albumText.getText().toString());
                    }

                    progressDialog.dismiss();

                    mCallback.onEditSongSave(true);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        mp3File = null;

        try {
            File f = new File(HomeActivity.editSong.getPath());
            mp3File = (MP3File) AudioFileIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        }

        if (mp3File == null) {
            Toast.makeText(ctx, "Error in loading the file", Toast.LENGTH_SHORT).show();
            mCallback.onEditSongSave(false);
        }
        if (!mp3File.hasID3v2Tag()) {
            Toast.makeText(ctx, "No Tags Found", Toast.LENGTH_SHORT).show();
            mCallback.onEditSongSave(false);
        }

        if (mp3File != null && mp3File.hasID3v2Tag()) {
            Tag tag = mp3File.getTag();
            titleText.setText(tag.getFirst(FieldKey.TITLE));
            artistText.setText(tag.getFirst(FieldKey.ARTIST));
            albumText.setText(tag.getFirst(FieldKey.ALBUM));
        }
    }

    public Bitmap getBitmap(String url) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(url);
        Bitmap bitmap = null;

        byte[] data = mmr.getEmbeddedPicture();

        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        } else {
            return null;
        }
    }

}
