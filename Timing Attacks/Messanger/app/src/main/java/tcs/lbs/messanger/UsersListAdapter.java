package tcs.lbs.messanger;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder>
{
    private LayoutInflater inflater;
    private ItemClickListener clickListener;
    private List<User> userList;

    // Data is passed into the constructor
    public UsersListAdapter(Context _context, List<User> _userList)
    {
        this.inflater = LayoutInflater.from(_context);
        this.userList = _userList;
    }

    // Inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        User currentUser = userList.get(position);
        holder.nameTextView.setText(currentUser.getUserName());
        holder.userAvatar.setImageResource(currentUser.getUserAvatar());
    }

    // Total number of rows
    @Override
    public int getItemCount()
    {
        return userList.size();
    }


    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener
    {
        TextView nameTextView;
        ImageView userAvatar;

        ViewHolder(View itemView)
        {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name_textView);
            userAvatar = itemView.findViewById(R.id.user_avatar_ImageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (clickListener != null)
                clickListener.onUserClick(view, getAdapterPosition());
        }
    }

    // A method for getting data at click position
    User getItem(int id)
    {
        return userList.get(id);
    }

    // Allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener)
    {
        this.clickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener
    {
        void onUserClick(View view, int position);
    }
}