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

/**
 * Adapter class for comments
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<CommentData> comments = new ArrayList<>(); // Use CommentData here

    /**
     * Create of our comment view holder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(itemView);
    }

    /**
     * What to do when the view holder is inflated
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        CommentData comment = comments.get(position);
        holder.commentText.setText(comment.getText());
        holder.displayName.setText(comment.getDisplayName());
//        holder.username.setText(comment.getUsername());
        holder.username.setText("@" + comment.getUsername());
        // Format and set the timestamp
        holder.timestampText.setText(comment.getFormattedTimestamp());


    }

    /**
     * Getter for number of comments
     * @return number of comments
     */
    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * Setter for the comments list
     * @param comments
     */
    public void setComments(List<CommentData> comments) {  // Use CommentData here
        this.comments = comments;
        notifyDataSetChanged();
    }

    /**
     * Where we create our comment view holder
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView timestampText;
        TextView commentText;
        TextView username;
        TextView displayName;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            username = itemView.findViewById(R.id.username);
            displayName = itemView.findViewById(R.id.display_name);
            timestampText = itemView.findViewById(R.id.time);
        }
    }
}
