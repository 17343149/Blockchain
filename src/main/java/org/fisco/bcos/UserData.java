package org.fisco.bcos;

public class UserData{
    private final String name;
    private final String count;
    private final String oweName;

    public UserData(String a, String b, String c){
        name = a;
        count = b;
        oweName = c;
    }

    public String getName(){
        return name;
    }

    public String getCount(){
        return count;
    }

    public String getOweName(){
        return oweName;
    }
}