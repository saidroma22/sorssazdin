package com.Azzadine.offline;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.Azzadine.offline.SongFragment.OnListSongsFragmentInteractionListener;
import com.Azzadine.offline.dummy.DummyContent.DummyItem;

import java.util.List;

public class MysongRecyclerViewAdapter extends RecyclerView.Adapter<MysongRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListSongsFragmentInteractionListener mListener;

    public MysongRecyclerViewAdapter(List<DummyItem> items, OnListSongsFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        DummyItem item = mValues.get(position);
        holder.mItem = item;

        // إعادة تعيين كل القيم بشكل واضح
        holder.songNumber.setVisibility(View.VISIBLE);
        holder.songNumber.setText(String.valueOf(position + 1));
        holder.title.setText(item.content != null ? item.content : "");
        holder.duration.setText(item.timeMusic != null ? item.timeMusic : "00:00");

        // إعادة ضبط الخلفية (اختياري لتجنب فقدان الظل أو المشاكل في CardView)
        holder.mView.setBackgroundResource(R.drawable.bg_song_item);

        // التعامل مع الضغط
        holder.mView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onListSongsFragmentInteraction(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView title;
        public final TextView duration;
        public final TextView songNumber;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = view.findViewById(R.id.song_title);
            duration = view.findViewById(R.id.song_duration);
            songNumber = view.findViewById(R.id.song_number);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + title.getText() + "'";
        }
    }
}
