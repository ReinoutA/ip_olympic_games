package instances.wrappers;

import instances.Task;
import instances.Skill;
import instances.Volunteer;

import java.util.List;

public class TaskSkillVolunteers {
    private Task task;
    private Skill skill;
    private List<Volunteer> volunteers;

    public TaskSkillVolunteers(Task task, Skill skill, List<Volunteer> volunteers) {
        this.task = task;
        this.skill = skill;
        this.volunteers = volunteers;
    }

    public Task getTask() {
        return task;
    }

    public Skill getSkill() {
        return skill;
    }

    public List<Volunteer> getVolunteers() {
        return volunteers;
    }
}
