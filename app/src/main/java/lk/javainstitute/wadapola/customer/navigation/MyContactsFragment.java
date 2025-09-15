package lk.javainstitute.wadapola.customer.navigation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.SQLiteHelper;


public class MyContactsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_contacts, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerContact);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                ContactItemAdapter.ContactItemViewHolder viewHolder1 = (ContactItemAdapter.ContactItemViewHolder) viewHolder;

                //refresh
//                NoteListAdapter noteListAdapter = (NoteListAdapter) recyclerView1.getAdapter();
//                noteListAdapter.removeItem(viewHolder.getAdapterPosition());

//                ContactItemAdapter contactItemAdapter = (ContactItemAdapter) recyclerView.getAdapter();
//                contactItemAdapter.rem
//                int adapterPosition = viewHolder1.getAdapterPosition();
//                contactItemAdapter.notifyItemRemoved(adapterPosition);

                int position = viewHolder.getAdapterPosition();
                ContactItemAdapter adapter = (ContactItemAdapter) recyclerView.getAdapter();

                if (adapter != null) {
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(view.getContext(), "mycontact.db", null, 1);
                    adapter.removeItem(position, sqLiteHelper);
                }

//                SQLiteHelper sqLiteHelper = new SQLiteHelper(view.getContext(), "mycontact.db", null, 1);
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();
//                        int row = sqLiteDatabase.delete("contact",
//                                "`worker_id`=?",
//                                new String[]{viewHolder1.worker_id}
//                        );
//                        Log.i("Log1", row + " Row Deleted");
//                    }
//                }).start();

            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        SQLiteHelper sqLiteHelper = new SQLiteHelper(view.getContext(), "mycontact.db", null, 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase readableDatabase = sqLiteHelper.getReadableDatabase();
                Cursor cursor = readableDatabase.query("contact"
                        , null, null, null, null, null
                        , "`worker_id` DESC");
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(new ContactItemAdapter(getContext(),cursor));
                    }
                });

            }
        }).start();
    }

}

class ContactItemAdapter extends RecyclerView.Adapter<ContactItemAdapter.ContactItemViewHolder> {
    private Context context;
    Cursor cursor;
    public ContactItemAdapter(Context context , Cursor cursor) {
        this.cursor = cursor;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflate = layoutInflater.inflate(R.layout.my_contact_item, parent, false);
        return new ContactItemViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactItemViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.worker_id = cursor.getString(0);
        String fName = cursor.getString(1);
        String lName = cursor.getString(2);
        String mobile = cursor.getString(3);
        String email = cursor.getString(4);
        String type = cursor.getString(5);

        holder.textViewName.setText(fName + " " + lName);
        holder.textViewType.setText(type);
        holder.textViewEmail.setText(email);
        holder.textViewMobile.setText(mobile);
//        holder.textViewLetter.setText(fName.charAt(0));
        // Check if fName is not empty/null and set the first letter
        if (fName != null && !fName.isEmpty()) {
            holder.textViewLetter.setText(String.valueOf(fName.charAt(0)).toUpperCase());
        } else {
            holder.textViewLetter.setText("#"); // Default character if name is missing
        }

        holder.callImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), mobile, Toast.LENGTH_SHORT).show();

                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    Uri uri = Uri.parse("tel:" + mobile);
                    intent.setData(uri);
                    view.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
    public void removeItem(int position, SQLiteHelper sqLiteHelper) {
        if (cursor.moveToPosition(position)) {
            String workerId = cursor.getString(0); // Get worker_id
            SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
            db.delete("contact", "`worker_id`=?", new String[]{workerId});
            db.close();
        }

        // Refresh the cursor with the updated data
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor newCursor = db.query("contact", null, null, null, null, null, "`worker_id` DESC");

        updateCursor(newCursor); // Update RecyclerView with new data
    }

    public void updateCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close(); // Close old cursor to prevent memory leaks
        }
        cursor = newCursor;
        notifyDataSetChanged(); // Refresh RecyclerView
    }


    static class ContactItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewType, textViewEmail, textViewMobile, textViewLetter;
        ImageView callImage;
        String worker_id;

        public ContactItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewConName);
            textViewType = itemView.findViewById(R.id.textViewConType);
            textViewEmail = itemView.findViewById(R.id.textViewConEmail);
            textViewMobile = itemView.findViewById(R.id.textViewConMobile);
            textViewLetter = itemView.findViewById(R.id.singleText);
            callImage = itemView.findViewById(R.id.imageViewCallContact);
        }
    }

}