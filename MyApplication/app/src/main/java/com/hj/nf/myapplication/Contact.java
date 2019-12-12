package com.hj.nf.myapplication;

/**
 * Created by snell1 on 2017-02-28.
 */

// list에 사용될 개체들 즉 Contact를 따로 만들어줍니다
public class Contact {
    String name;   // 이름
    String phone;  // 번호
    boolean isCheck = false; // checkbox 인자값

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;

    }
}
