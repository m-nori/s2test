package example.logic;

import example.service.DummyService;

public class DummyLogic {
	public DummyService dummyService;

	public String execute() {
		return dummyService.say();
	}
}
