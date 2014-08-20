package de.stephanlindauer.criticalmass_berlin.helper;

public interface ICommand {
    public void execute(String... payload);
}