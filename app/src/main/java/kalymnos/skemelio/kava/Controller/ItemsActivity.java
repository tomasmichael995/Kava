package kalymnos.skemelio.kava.Controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import kalymnos.skemelio.kava.Model.Category;
import kalymnos.skemelio.kava.Model.Item;
import kalymnos.skemelio.kava.R;
import kalymnos.skemelio.kava.View.screen_items.ItemsScreenViewMvc;
import kalymnos.skemelio.kava.View.screen_items.ItemsScreenViewMvcImpl;

public class ItemsActivity extends AppCompatActivity
        implements ItemsScreenViewMvc.OnSaveClickListener,
        ItemsScreenViewMvc.OnItemQuantityChangeListener {

    private ItemsScreenViewMvc viewMvc;
    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCategory();
        initViewMvc();
        setContentView(viewMvc.getRootView());
        getSupportActionBar().setTitle(category.title+" ("+category.getItemList().size()+")");
        viewMvc.bindTitle(getString(R.string.take_notes_on_items) + String.format(" \"%s\"", category.title));
        viewMvc.bindItems(category.getItemList());
    }

    private void initCategory() {
        Bundle data = getIntent().getExtras();
        category = (Category) data.getSerializable(Category.TAG);
    }

    private void initViewMvc() {
        viewMvc = new ItemsScreenViewMvcImpl(LayoutInflater.from(this), null);
        viewMvc.setOnItemQuantityChangeListener(this);
        viewMvc.setOnSaveClickListener(this);
    }

    @Override
    public void onAtomAdded(int position) {
        Item item = category.getItemAt(position);
        item.addAtom();
        viewMvc.bindItems(category.getItemList());
    }

    @Override
    public void onContainerAdded(int position) {
        Item item = category.getItemAt(position);
        item.addContainer();
        viewMvc.bindItems(category.getItemList());
    }

    @Override
    public void onAtomRemoved(int position) {
        Item item = category.getItemAt(position);
        item.removeAtom();
        viewMvc.bindItems(category.getItemList());
    }

    @Override
    public void onContainerRemoved(int position) {
        Item item = category.getItemAt(position);
        item.removeContainer();
        viewMvc.bindItems(category.getItemList());
    }

    @Override
    public void onSaveClick() {

    }
}
