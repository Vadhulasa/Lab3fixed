package tcs.lbs.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder>
{
    private ArrayList<NoteItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;


    // Data is passed into the constructor
    NotesAdapter(Context context, ArrayList<NoteItem> data)
    {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }


    // Inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.note_row, parent, false);
        return new ViewHolder(view);
    }

    // Binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        NoteItem currentNote = mData.get(position);
        holder.notesIDTextView.setText(currentNote.getID());
        holder.notesTextTextView.setText(currentNote.getText());
    }

    // Total number of rows
    @Override
    public int getItemCount()
    {
        return mData.size();
    }


    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView notesTextTextView, notesIDTextView;

        ViewHolder(View itemView)
        {
            super(itemView);
            notesTextTextView = itemView.findViewById(R.id.noteItemText);
            notesIDTextView = itemView.findViewById(R.id.noteItemId);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    // A method for getting data at click position
    NoteItem getItem(int id)
    {
        return mData.get(id);
    }

    // Allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}


