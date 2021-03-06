package kalymnos.skemelio.kava.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import kalymnos.skemelio.kava.R;
import kalymnos.skemelio.kava.dialogs.AddTitleDialog;
import kalymnos.skemelio.kava.model.Category;
import kalymnos.skemelio.kava.persistance.KavaRepo;
import kalymnos.skemelio.kava.persistance.KavaRepoImpl;
import kalymnos.skemelio.kava.persistance.TitleRepo;
import kalymnos.skemelio.kava.persistance.TitleRepoImpl;
import kalymnos.skemelio.kava.util.CheckoutFormatter;
import kalymnos.skemelio.kava.util.Time;
import kalymnos.skemelio.kava.view.screen_checkout.CheckoutScreenViewMvc;
import kalymnos.skemelio.kava.view.screen_checkout.CheckoutScreenViewMvcImpl;

public class CheckoutActivity
        extends AppCompatActivity
        implements CheckoutScreenViewMvc.OnShareListener,
        AddTitleDialog.AddTitleDialogListener {

    private CheckoutScreenViewMvc viewMvc;
    private List<Category> categories;
    private CheckoutFormatter formatter;
    private KavaRepo kavaRepo;
    private TitleRepo titleRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleRepo = new TitleRepoImpl(this);
        kavaRepo = KavaRepoImpl.createFrom(this);
        initCategories();
        initFormatter();
        setupUI();
    }

    private void initCategories() {
        Parcelable[] parcels =
                getIntent().getParcelableArrayExtra(Category.class.getSimpleName());
        categories = new ArrayList<>();
        for (int i = 0; i < parcels.length; i++) {
            Category c = (Category) parcels[i];
            if (!kavaRepo.isEmpty(c))
                categories.add(c);
        }
    }

    private void initFormatter() {
        formatter = new CheckoutFormatter(
                kavaRepo,
                categories.toArray(new Category[categories.size()]),
                getString(R.string.atoms),
                getString(R.string.containers));
    }

    private void setupUI() {
        initViewMvc();
        getSupportActionBar().setTitle(R.string.checkout_send);
        addTitle();
        setContentView(viewMvc.getRootView());
    }

    private void initViewMvc() {
        viewMvc = new CheckoutScreenViewMvcImpl(LayoutInflater.from(this), null);
        viewMvc.setOnShareListener(this);
        viewMvc.add(categories);
    }

    private void addTitle() {
        String title = titleRepo.loadTitle();
        if (title != null) {
            viewMvc.setTitle(title);
            viewMvc.showTitle();
        } else {
            viewMvc.hideTitle();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.checkout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_title) {
            showTitleDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTitleDialog() {
        AddTitleDialog d = new AddTitleDialog();
        d.setAddTitleDialogListener(this);
        String tag = "" + d.hashCode();
        d.show(getSupportFragmentManager(), tag);
    }

    @Override
    public void onShare() {
        Intent share = getShareIntent();
        startActivity(Intent.createChooser(share, getString(R.string.share_the_noted_items)));
    }

    private Intent getShareIntent() {
        String finalText = formatter.createShareableText(
                viewMvc.getTitle(),
                Time.getCurrentTime(),
                formatter.formatKava());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, finalText);
        return intent;
    }

    @Override
    public void onDialogPositiveClick(String title) {
        title = title.trim();
        boolean titleNotExist = title == null || title.length() == 0;
        if (titleNotExist) {
            viewMvc.hideTitle();
            titleRepo.clear();
        } else {
            viewMvc.setTitle(title);
            viewMvc.showTitle();
            titleRepo.save(title);
        }
    }
}
