package de.stephanlindauer.criticalmassberlin.helper;

public interface ICommand {
    public void execute(String... payload);
}