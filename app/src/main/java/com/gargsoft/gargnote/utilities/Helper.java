package com.gargsoft.gargnote.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.gargsoft.gargnote.R;

import java.util.ArrayList;

public class Helper {
    public interface ConfirmationListener {
        public void onConfirmation();
    }

    public interface SelectItemListener {
        public void onItemSelected(int position, boolean secondaryAction);
    }

    public interface SelectMultipleItemListener {
        public void onOK(ArrayList<Integer> selectedItems);
    }

    private Helper() {}

    public static void confirmation(Context context, int messageId, final ConfirmationListener listener) {
        confirmation(context, context.getResources().getString(messageId), listener);
    }

    public static void confirmation(Context context, CharSequence message, final ConfirmationListener listener) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.confirmation)
                .setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onConfirmation();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public static void selectItem(Context context, CharSequence title, ArrayList<Object> items, int defaultSelection, CharSequence primaryActionButtonText, CharSequence secondaryActionButtonText, final SelectItemListener listener) {
        if (items.size() <= 0)
            return;

        CharSequence[] textItems = items.toArray(new CharSequence[items.size()]);

        if (defaultSelection < 0 || defaultSelection >= items.size())
            defaultSelection = 0;

        // This must be final so it needs to be an array so we can just set the first element in the listeners.
        final int[] selectedItem = new int[] {defaultSelection};

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setSingleChoiceItems(textItems, defaultSelection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItem[0] = which;
                    }
                })
                .setPositiveButton(TextUtils.isEmpty(primaryActionButtonText) ? context.getResources().getString(R.string.ok) : primaryActionButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onItemSelected(selectedItem[0], false);
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        if (!TextUtils.isEmpty(secondaryActionButtonText)) {
            builder.setNeutralButton(secondaryActionButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onItemSelected(selectedItem[0], true);
                }
            });
        }

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }

    public static void selectMultipleItems(Context context, CharSequence title, ArrayList<Object> items, ArrayList<Integer> selectedItems, final SelectMultipleItemListener listener) {
        if (items.size() <= 0)
            return;

        CharSequence[] textItems = items.toArray(new CharSequence[items.size()]);
        final boolean[] checkedItems = new boolean[items.size()];

        for (int i = 0; i < items.size(); ++i)
            checkedItems[i] = false;

        for (Integer index : selectedItems)
            checkedItems[index] = true;

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMultiChoiceItems(textItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Integer> selectedItems = new ArrayList<>();

                        for (int i = 0; i < checkedItems.length; ++i) {
                            if (checkedItems[i])
                                selectedItems.add(i);
                        }

                        dialog.dismiss();
                        listener.onOK(selectedItems);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public static String makePath(CharSequence path, CharSequence name) {
        String fullPath = path.toString();

        if (fullPath.length() > 0 && fullPath.charAt(fullPath.length() - 1) != '\\')
            fullPath += '\\';

        fullPath += name;

        return fullPath;
    }

    public static int dpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static void requestDefaultEditFocus(final Context context, final EditText input) {
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                input.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager= (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });

        input.requestFocus();
    }

    public static void toastSaveFailed(Context context) {
        Toast.makeText(context, R.string.savefailed, Toast.LENGTH_SHORT).show();
    }
}
