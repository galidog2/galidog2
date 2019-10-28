package com.example.galidog2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {


        private static final String TAG = "RecyclerViewAdapter";
        private List<String> mTrajets;
        private OnTrajetListener mOnTrajetListener;


        // Constructeur de l'adapter du recyclerview
        public RecyclerViewAdapter(List<String> NomTrajets, OnTrajetListener onTrajetListener){
            this.mTrajets = NomTrajets;
            this.mOnTrajetListener = onTrajetListener;
        }

        @NonNull
        // La création de l'Adapter provoque la création du ViewHolder
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent,false);
            return new ViewHolder(view,mOnTrajetListener);
        }

        public int getItemCount() {
            return mTrajets.size();
        }

        // On relie le ViewHolder à l'Item affiché par l'adapter.
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.nomListe.setText(mTrajets.get(position));
        }

        public void show(List<String> mesTrajets){
            mTrajets = mesTrajets;
            notifyDataSetChanged();
        }


        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView nomListe;
            CoordinatorLayout parentLayout;
            OnTrajetListener mOnTrajetListener;

            // le ViewHolder permet d'ordonner la liste des item
            ViewHolder(@NonNull View itemView, OnTrajetListener onTrajetListener) {
                super(itemView);
                nomListe=itemView.findViewById(R.id.item);
                parentLayout=itemView.findViewById(R.id.parent_layout);
                this.mOnTrajetListener = onTrajetListener;

                itemView.setOnClickListener(this);
            }

            @Override
            // lors d'un clique, on appelle la méthode onListClick de l'interface OnListListener
            public void onClick(View v) {
                mOnTrajetListener.onTrajetClick(getAdapterPosition());
            }
        }


    // Déclaration de l'interface
    public interface OnTrajetListener{
            void onTrajetClick(int position);
        }

}
