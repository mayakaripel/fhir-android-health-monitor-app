package edu.monash.kmhc.view;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.monash.kmhc.MainActivity;
import edu.monash.kmhc.R;


public class LoginFragment extends Fragment {

    private EditText practitionerID;
    private Button signInButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login_fragment, container, false);

        practitionerID = root.findViewById(R.id.login_et_pracID);
        signInButton = root.findViewById(R.id.login_btn_login);

        signInButton.setOnClickListener(signInButtonListener);
        // Inflate the layout for this fragment
        return root;
    }

    View.OnClickListener signInButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (practitionerID.getText().toString().equals("")){
                Context context = getContext();
                Toast toast = Toast.makeText(context, R.string.invalid_username, Toast.LENGTH_LONG);
                toast.show();
            }
            else{
                MainActivity main = (MainActivity) getActivity();
                main.newSelectPatientFragment(MainActivity.select_patients_fragment,practitionerID.getText().toString());
            }

        }
    };
}