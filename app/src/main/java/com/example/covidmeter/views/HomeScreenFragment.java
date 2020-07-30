package com.example.covidmeter.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.covidmeter.MainActivity;
import com.example.covidmeter.R;
import com.example.covidmeter.controllers.SessionController;
import com.example.covidmeter.models.Info;
import com.example.covidmeter.models.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeScreenFragment extends Fragment implements  SwipeRefreshLayout.OnRefreshListener {

    private User user;
    private List<String> infoStates;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private InfoListAdapter infoListAdapter;

    public HomeScreenFragment(User user) {
        this.user = user;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);
        TextView welcome_text = view.findViewById(R.id.welcome_text);
        TextView location_text = view.findViewById(R.id.location_text);
        TextView symptoms_text = view.findViewById(R.id.nmr_symptoms_text);
        recyclerView = view.findViewById(R.id.infoRecycler);
        swipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);


        infoStates = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Info_texts)));


        String welcome = "Olá " + this.user.getName() + "!";
        welcome_text.setText(welcome);
        location_text.setText(this.user.getLocation());

        if (user.getHealthState().getSymptoms().get(0).getName().equals("Sem sintomas"))
            symptoms_text.setText(String.format("%s   %s", "Sem sintomas", this.user.getHealthState().getState()));
        else {
            symptoms_text.setText(String.format(Locale.getDefault(),"%d sintomas   %s",this.user.getHealthState().getSymptoms().size(), this.user.getHealthState().getState()));
        }

        infoListAdapter = new HomeScreenFragment.InfoListAdapter();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.getContext(), 2);


        recyclerView.setAdapter(infoListAdapter);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    public void callUpdateFragment(View view){

    }


    @Override
    public void onRefresh() {
        SessionController sessionController = SessionController.getInstance();
        sessionController.getAreaInformation();
        sessionController.getInformation().observe(this, new Observer<List<Info>>() {
            @Override
            public void onChanged(List<Info> infos) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                updateDashboard(infos);
            }
        });
    }


    private void updateDashboard(List<Info> infos){
        infoListAdapter.updateInfo(infos);
    }

    class InfoListAdapter extends RecyclerView.Adapter<HomeScreenFragment.InfoListAdapter.SymptomViewHolder> {

        private int[] images = {R.drawable.semsintomas, R.drawable.comsintomas,R.drawable.suspeitos, R.drawable.infetados};
        private List<Info> infoList;


        public InfoListAdapter() {
            infoList = SessionController.getInstance().getInformation().getValue();
        }

        public void updateInfo(List<Info> newInfos){
            infoList=new ArrayList<>();
            infoList.addAll(newInfos);
            notifyDataSetChanged();
        }


        @NonNull
        @Override
        public HomeScreenFragment.InfoListAdapter.SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.getContext());
            View itemView = inflater.inflate(R.layout.info_item, parent, false);
            return new HomeScreenFragment.InfoListAdapter.SymptomViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final HomeScreenFragment.InfoListAdapter.SymptomViewHolder holder, final int position) {
            holder.typePhoto.setBackgroundResource(images[position]);
            holder.infoNumber.setText(String.valueOf(infoList.get(position).getQtdInfo()));
            holder.infoType.setText(infoList.get(position).getTypeInfo());
        }


        @Override
        public int getItemCount() {
            return infoStates.size();
        }

        class SymptomViewHolder extends RecyclerView.ViewHolder {

            ImageView typePhoto;
            TextView infoNumber, infoType;

            public SymptomViewHolder(@NonNull View itemView) {
                super(itemView);
                typePhoto=itemView.findViewById(R.id.type_photo);
                infoNumber = itemView.findViewById(R.id.info_number);
                infoType = itemView.findViewById(R.id.info_type);
            }
        }


    }

}
