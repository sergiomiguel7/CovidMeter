package com.example.covidmeter.views;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.covidmeter.MainActivity;
import com.example.covidmeter.R;
import com.example.covidmeter.controllers.SessionController;
import com.example.covidmeter.models.Symptom;
import com.example.covidmeter.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateStateFragment extends Fragment {

    private List<Symptom> symptomsList, selectedSymptoms;
    private String state;
    private RadioGroup radioGroup;
    private RecyclerView recyclerView;
    private Button button;
    private ProgressBar progressBar;


    public UpdateStateFragment(List<Symptom> symptoms) {
        this.symptomsList = symptoms;
        this.selectedSymptoms = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update_state, container, false);
        recyclerView = view.findViewById(R.id.symptomsRecycler);
        button = view.findViewById(R.id.confirm_button);
        radioGroup = view.findViewById(R.id.radio_group);
        progressBar = view.findViewById(R.id.progress_bar);

        handleRadioGroup();

        SymptomsListAdapter recyclerViewAdapter = new SymptomsListAdapter();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.getContext(), 2);


        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }


    /*
    in this method create a radio group with the options available in the string.xml referent to the state of user
    including the onClickListener to check if a option was selected
     */
    private void handleRadioGroup() {
        List<String> stateList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.List_choices)));
        int i = 0;
        for (final String state : stateList) {
            RadioButton radioButton = new RadioButton(MainActivity.getContext());
            radioButton.setText(state);
            radioButton.setId(i);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled}, //disabled
                            new int[]{android.R.attr.state_enabled} //enabled
                    },
                    new int[]{
                            Color.BLACK, //disabled
                            Color.parseColor("#1D29A3") //enabled
                    }
            );
            radioButton.setButtonTintList(colorStateList);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = ((RadioButton) v).isChecked();
                    if (checked) {
                        updateState(state);
                    }
                }
            });
            this.radioGroup.addView(radioButton);
            i++;
        }
    }

    private void uncheckAll() {

        for (int i = 1; i < recyclerView.getChildCount(); i++) {
            LinearLayout l = null;
            View checkBox = null;
            l = (LinearLayout) recyclerView.getChildAt(i);
            checkBox = l.getChildAt(1);
            ((CheckBox) checkBox).setChecked(false);
        }

    }

    private void updateState(String state) {
        this.state = state;
    }

    /*
    when the user clicks the confirm button it checks everything and update the user state(SessionController)
     */
    public void updateUserSymptoms(View view) {
        User user = SessionController.getInstance().getUser();
        List<String> stateList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.List_choices)));


        if (selectedSymptoms.size() > 0 && state != null) {
            if (user.getHealthState() != null) {
                String actualState = user.getHealthState().getState();
                if (actualState.equals(stateList.get(0))) {
                    if (state.equals(stateList.get(0)) || state.equals(stateList.get(2))) {
                        confirmData();
                    } else {
                        Toast.makeText(MainActivity.getContext(), String.format("O seu estado anterior era %s \nEstado escolhido incompativel", actualState), Toast.LENGTH_SHORT).show();
                    }
                } else if (actualState.equals(stateList.get(2))) {
                    if (state.equals(stateList.get(0)) || state.equals(stateList.get(1)) || state.equals(stateList.get(2))) {
                        confirmData();
                    } else {
                        Toast.makeText(MainActivity.getContext(), String.format("O seu estado anterior era %s \nEstado escolhido incompativel", actualState), Toast.LENGTH_SHORT).show();
                    }
                } else if (actualState.equals(stateList.get(1))) {
                    if (state.equals(stateList.get(1)) || state.equals(stateList.get(3))) {
                        confirmData();
                    } else {
                        Toast.makeText(MainActivity.getContext(), String.format("O seu estado anterior era %s \nEstado escolhido incompativel", actualState), Toast.LENGTH_SHORT).show();
                    }
                } else if (actualState.equals(stateList.get(3))) {
                    if (state.equals(stateList.get(3))) {
                        confirmData();
                    } else {
                        Toast.makeText(MainActivity.getContext(), String.format("O seu estado anterior era %s \nUnico estado possivel é o atual", actualState), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                confirmData();
            }
        } else
            Toast.makeText(MainActivity.getContext(), "Nenhuma opção selecionada", Toast.LENGTH_SHORT).show();
    }

    public void confirmData() {
        SessionController.getInstance().updateUserState(selectedSymptoms, state);
        button.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }


    class SymptomsListAdapter extends RecyclerView.Adapter<SymptomsListAdapter.SymptomViewHolder> {


        public SymptomsListAdapter() {
        }


        @NonNull
        @Override
        public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.getContext());
            View itemView = inflater.inflate(R.layout.symptom_item, parent, false);
            return new SymptomViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final SymptomViewHolder holder, final int position) {
            holder.symptom_name.setText(symptomsList.get(position).getName());

            //todo: se o sem sintomas estiver selecionado, tirar os outros todos
            /*
            if the checkbox was selected the symptom is added to a list where the user saves is information
             */
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isChecked = holder.checkBox.isChecked();
                    for (int i = 0; i < symptomsList.size(); i++) {
                        //if "Sem sintomas" selected selected symptoms is cleaned and all checkboxes unchecked
                        if (selectedSymptoms.contains(symptomsList.get(0))) {
                            uncheckAll();
                            selectedSymptoms = new ArrayList<>();
                            selectedSymptoms.add(symptomsList.get(0));
                        }
                        if (isChecked) {
                            if (!selectedSymptoms.contains(symptomsList.get(position)) && !selectedSymptoms.contains(symptomsList.get(0))) {
                                if (position==0)
                                    uncheckAll();
                                selectedSymptoms.add(i, symptomsList.get(position));
                                Log.d("added", symptomsList.get(position).getName());
                                break;
                            }
                        } else {
                            selectedSymptoms.remove(symptomsList.get(position));
                            Log.d("removed", symptomsList.get(position).getName());
                            break;
                        }

                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return symptomsList.size();
        }

        class SymptomViewHolder extends RecyclerView.ViewHolder {

            TextView symptom_name;
            CheckBox checkBox;

            public SymptomViewHolder(@NonNull View itemView) {
                super(itemView);
                symptom_name = itemView.findViewById(R.id.symptom_name);
                checkBox = itemView.findViewById(R.id.checkbox);
            }
        }


    }
}
