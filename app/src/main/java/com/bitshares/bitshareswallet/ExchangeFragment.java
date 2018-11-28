package com.bitshares.bitshareswallet;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExchangeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExchangeFragment extends BaseFragment {

    private OnFragmentInteractionListener mListener;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private BtsFragmentPageAdapter mExchangeFragmentPageAdapter;

    private OrdersFragment mOrdersFragment;
    public ExchangeFragment() {}

    public static ExchangeFragment newInstance() {
        ExchangeFragment fragment = new ExchangeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        mTabLayout = view.findViewById(R.id.tabLayout);
        mTabLayout.setBackgroundColor(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("color", Color.parseColor("#303F9F")));
        mViewPager = view.findViewById(R.id.exchangeViewPager);
        mExchangeFragmentPageAdapter = new BtsFragmentPageAdapter(getFragmentManager(), false);
        mExchangeFragmentPageAdapter.addFragment(TransactionSellBuyFragment.newInstance(TransactionSellBuyFragment.TRANSACTION_BUY), getResources().getString(R.string.label_buy));
        mExchangeFragmentPageAdapter.addFragment(TransactionSellBuyFragment.newInstance(TransactionSellBuyFragment.TRANSACTION_SELL), getResources().getString(R.string.label_sell));
        mOrdersFragment = OrdersFragment.newInstance();
        mExchangeFragmentPageAdapter.addFragment(mOrdersFragment, getResources().getString(R.string.title_my_orders));
        mViewPager.setAdapter(mExchangeFragmentPageAdapter);
        initPager(mViewPager, mExchangeFragmentPageAdapter);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() != 2) {
                    MainActivity.hideSoftKeyboard(mTabLayout, getActivity());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
