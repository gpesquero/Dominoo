package org.dominoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class DropdownButton extends android.widget.Button implements View.OnClickListener{

    private Context mContext = null;

    OnSelectionChangedListener mListener = null;

    private PopupWindow mPopupWindow = null;

    private ListView mListView = null;

    private ArrayList<String> mItemsList = new ArrayList<String>();

    private ArrayAdapter<String> mAdapter = null;

    interface OnSelectionChangedListener {

        void onSelectionChanged(View view, int position, String selectedItemText);
    }

    public DropdownButton(Context context) {
        super(context);

        mContext = context;

        initPopupWindow(context);

        setOnClickListener(this);
    }

    public DropdownButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        initPopupWindow(context);

        setOnClickListener(this);
    }

    public DropdownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        initPopupWindow(context);

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (mPopupWindow != null) {

            mPopupWindow.showAsDropDown(v, -5, 0);
        }
    }

    public void setItemsList(ArrayList<String> itemsList) {

        mItemsList = itemsList;

        mAdapter = getAdapter(mContext, mItemsList);

        mListView.setAdapter(mAdapter);
    }

    public void setSelection(String text) {

        int pos = mItemsList.indexOf(text);

        if (pos < 0) {

            return;
        }

        setText(text);
    }

    String getSelectedItemText() {

        return getText().toString();
    }

    private void initPopupWindow(Context context) {

        // convert to simple array


        // initialize a pop up window type
        //PopupWindow popupWindow = new PopupWindow(context);
        mPopupWindow = new PopupWindow(context);

        // the drop down list is a list view
        mListView = new ListView(context);

        mItemsList = new ArrayList<String>();

        mAdapter = getAdapter(context, mItemsList);

        // set our adapter and pass our pop up window contents
        mListView.setAdapter(mAdapter);

        // set the item click listener
        mListView.setOnItemClickListener(new DropdownOnItemClickListener(this));

        // some other visual settings
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(250);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        mPopupWindow.setContentView(mListView);
    }

    private ArrayAdapter<String> getAdapter(final Context context,
                                            ArrayList<String> itemsList) {

        String itemsArray[] = new String[itemsList.size()];
        itemsList.toArray(itemsArray);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, itemsArray) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                String itemText = getItem(position);

                TextView listItem = new TextView(context);

                listItem.setText(itemText);
                //listItem.setTag(id);
                listItem.setTextSize(22);
                listItem.setPadding(10, 10, 10, 10);
                listItem.setTextColor(Color.WHITE);

                return listItem;
            }
        };

        return adapter;
    }

    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {

        DropdownButton mDropdownButton = null;

        public DropdownOnItemClickListener(DropdownButton dropdownButton) {

            mDropdownButton = dropdownButton;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // get the context and main activity to access variables
            //Context mContext = v.getContext();
            //MainActivity mainActivity = ((MainActivity) mContext);

            // add some animation when a list item was clicked
            Animation fadeInAnimation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setDuration(10);
            view.startAnimation(fadeInAnimation);

            // dismiss the pop up
            //mainActivity.popupWindowDogs.dismiss();
            mPopupWindow.dismiss();

            String selectedItemText = ((TextView) view).getText().toString();

            // Set the text of the dropdown button
            setText(selectedItemText);

            if (mListener != null) {

                mListener.onSelectionChanged(mDropdownButton, position, selectedItemText);
            }
            // get the text and set it as the button text

            //mainActivity.buttonShowDropDown.setText(selectedItemText);


            // get the id
            //String selectedItemTag = ((TextView) v).getTag().toString();
            //Toast.makeText(mContext, "Dog ID is: " + selectedItemTag, Toast.LENGTH_SHORT).show();

        }

    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {

        mListener = listener;
    }
}
