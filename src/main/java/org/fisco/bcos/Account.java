package org.fisco.bcos;

import java.util.ArrayList;
import java.util.List;

public class Account{
    public int code;
    public String msg;
    public int count;
    public List<UserData> data;

    public Account(){
        code = 0;
        msg = "";
        count = 3;
        data = new ArrayList<UserData>();
    }

}