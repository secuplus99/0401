package com.hj.nf.myapplication;

import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

/**
 * Created by snell1 on 2017-03-30.
 */

public class ContactAdapter3 extends BaseQuickAdapter<Contact, BaseViewHolder> implements Filterable {


    ArrayList<Contact> contacts; // 리스트
    private final ArrayList<Contact>  userList;     //검색을 위한 기존 리스트
    private final ArrayList<Contact>  filteredUserList;  //필터링 된 리스트
    private UserFilter userFilter;

    //리스트에 DATA 넣기
    public ContactAdapter3(ArrayList<Contact> data) {
        super(R.layout.list_obj, data);
        this.contacts = data;
        this.userList = data;
        this.filteredUserList = data;
    }

    //필터받기
    @Override
    public Filter getFilter() {
        if(userFilter == null)
            userFilter = new UserFilter(this, userList);
        return userFilter;
    }

    // 데이터 > 아이템을 설정한 list_obj 에 하나하나 집어 넣기
    @Override
    protected void convert(final BaseViewHolder helper, Contact item) {
        helper.setText(R.id.txt1_name, item.name)
                .setText(R.id.txt2_phone, item.phone);
        //체크박스 클릭
        helper.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contacts.get(helper.getLayoutPosition()).isCheck=!contacts.get(helper.getLayoutPosition()).isCheck; //전 상태와 비교
                notifyItemChanged(helper.getLayoutPosition()); //갱신
            }
        });
        helper.setChecked(R.id.checkBox, item.isCheck);


    }

    //필터구문
    private static class UserFilter extends Filter {

        private final ContactAdapter3 adapter;
        private final ArrayList<Contact> originalList;
        private final ArrayList<Contact> filteredList;

        //필터리스트와 기존리스트 생성
        private UserFilter(ContactAdapter3 adapter, ArrayList<Contact> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new ArrayList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        //필터링을 진행하고 result 반환
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList); //필터된게 없을경우, 검색값이 0
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final Contact data : originalList) {
                    if (data.name.contains(filterPattern) || data.phone.contains(filterPattern)) {
                        filteredList.add(data); //검색값 = 이름포함 or 검색값 = 번호포함
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        //필터된 리스트를 결과리스트로 갱신
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filteredUserList.clear();
            adapter.filteredUserList.addAll((ArrayList<Contact>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}