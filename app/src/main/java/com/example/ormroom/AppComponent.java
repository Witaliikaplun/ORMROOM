package com.example.ormroom;

import dagger.Component;

@Component(modules = {DaggerNetModule.class})
public interface AppComponent {
    void injectsToMainActivity(MainActivity mainActivity);
}
