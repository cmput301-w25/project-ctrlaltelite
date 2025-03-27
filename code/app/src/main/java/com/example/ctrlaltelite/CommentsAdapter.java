package com.example.ctrlaltelite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ctrlaltelite.CommentData;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<CommentData> comments = new ArrayList<>(); // Use CommentData here


    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        CommentData comment = comments.get(position);
        holder.commentText.setText(comment.getText());
        holder.username.setText(comment.getUsername());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<CommentData> comments) {  // Use CommentData here
        this.comments = comments;
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentText;
        TextView username;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            username = itemView.findViewById(R.id.username);
        }
    }
}
