package com.example.galidog2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.List;

public class LayoutManager {
    package com.example.myhello.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhello.R;
import com.example.myhello.data.models.ListeToDo;

import java.util.List;

    public class RecyclerViewAdapter1 extends RecyclerView.Adapter<RecyclerViewAdapter1.ViewHolder>{
        private static final String TAG = "RecyclerViewAdapter1";
        private List<ListeToDo> mNomListe;
        private OnListListener mOnListListener;


        // Constructeur de l'adapter du recyclerview
        public RecyclerViewAdapter1(List<ListeToDo> NomListe, OnListListener onListListener){
            this.mNomListe = NomListe;
            this.mOnListListener = onListListener;
        }

        @NonNull
        @Override
        // La création de l'Adapter provoque la création du ViewHolder
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent,false);
            return new ViewHolder(view,mOnListListener);
        }



        @Override
        public int getItemCount() {
            return mNomListe.size();
        }

        @Override
        // On relie le ViewHolder à l'Item affiché par l'adapter.
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.nomListe.setText(mNomListe.get(position).getTitreListeToDo());
        }

        public void show(List<ListeToDo> mesListesToDo){
            mNomListe = mesListesToDo;
            notifyDataSetChanged();
        }


        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView nomListe;
            CoordinatorLayout parentLayout;
            OnListListener mOnListListener;

            // le ViewHolder permet d'ordonner la liste des item
            ViewHolder(@NonNull View itemView, OnListListener onListListener) {
                super(itemView);
                nomListe=itemView.findViewById(R.id.item);
                parentLayout=itemView.findViewById(R.id.parent_layout);
                this.mOnListListener = onListListener;

                itemView.setOnClickListener(this);
            }

            @Override
            // lors d'un clique, on appelle la méthode onListClick de l'interface OnListListener
            public void onClick(View v) {
                mOnListListener.onListClick(getAdapterPosition());
            }
        }

        // Déclaration de l'interface
        public interface OnListListener{
            void onListClick(int position);
        }
    }


}
