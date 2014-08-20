package de.stephanlindauer.criticalmass.helper;

public interface ICommand {
    public void execute(String... payload);
}