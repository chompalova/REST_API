package com.restserver;

//bean class for handling JSON objects
public class Dog {
    private String name;
    private String breed;
    private int age;

    public Dog(String name, String breed) {
        this.name = name;
        this.breed = breed;
    }

    public Dog() {
    }

    public String getName() {
        return name;
    }

    public Dog setName(String name) {
        this.name = name;
        return this;
    }

    public String getBreed() {
        return breed;
    }


    public void setBreed(final String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public Dog setAge(int age) {
        this.age = age;
        return this;
    }
}
