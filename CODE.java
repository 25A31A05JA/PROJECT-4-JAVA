import java.util.*;

public class CODE {
    static class Job {
        String id;
        int duration, deadline, priority, arrival;

        Job(String id, int duration, int deadline, int priority, int arrival) {
            this.id = id;
            this.duration = duration;
            this.deadline = deadline;
            this.priority = priority;
            this.arrival = arrival;
        }
    }

    static class ScheduledJob {
        Job job;
        int completion;

        ScheduledJob(Job job, int completion) {
            this.job = job;
            this.completion = completion;
        }
    }

    static class Result {
        String algorithm;
        ArrayList<ScheduledJob> scheduled = new ArrayList<>();
        ArrayList<Job> missed = new ArrayList<>();
        int used, capacity;

        Result(String algorithm, int capacity) {
            this.algorithm = algorithm;
            this.capacity = capacity;
        }
    }

    static boolean isFeasible(ArrayList<Job> order, Job candidate,
                              int position, int capacity) {
        int elapsed = 0;
        for (int i = 0; i <= order.size(); i++) {
            if (i == position) {
                elapsed += candidate.duration;
                if (elapsed > candidate.deadline || elapsed > capacity)
                    return false;
            }
            if (i < order.size()) {
                Job job = order.get(i);
                elapsed += job.duration;
                if (elapsed > job.deadline || elapsed > capacity)
                    return false;
            }
        }
        return true;
    }

    static Result runGreedy(ArrayList<Job> input, int capacity) {
        ArrayList<Job> jobs = new ArrayList<>(input);
        jobs.sort((a, b) -> {
            if (a.priority != b.priority)
                return Integer.compare(b.priority, a.priority);
            if (a.deadline != b.deadline)
                return Integer.compare(a.deadline, b.deadline);
            return Integer.compare(a.duration, b.duration);
        });

        Result result = new Result("Greedy (priority-based)", capacity);
        ArrayList<Job> order = new ArrayList<>();

        for (Job job : jobs) {
            boolean placed = false;
            for (int pos = 0; pos <= order.size(); pos++) {
                if (isFeasible(order, job, pos, capacity)) {
                    order.add(pos, job);
                    placed = true;
                    break;
                }
            }
            if (!placed) result.missed.add(job);
        }

        int elapsed = 0;
        for (Job job : order) {
            elapsed += job.duration;
            result.scheduled.add(new ScheduledJob(job, elapsed));
        }
        result.used = elapsed;
        return result;
    }

    static Result runBaseline(ArrayList<Job> input, int capacity, String mode) {
        ArrayList<Job> jobs = new ArrayList<>(input);
        if (mode.equals("EDF")) {
            jobs.sort(Comparator.comparingInt(j -> j.deadline));
        } else {
            jobs.sort(Comparator.comparingInt(j -> j.arrival));
        }

        String name = mode.equals("EDF")
                ? "Earliest Deadline First"
                : "First Come First Served";
        Result result = new Result(name, capacity);
        int elapsed = 0;

        for (Job job : jobs) {
            if (elapsed + job.duration > capacity) {
                result.missed.add(job);
                continue;
            }
            elapsed += job.duration;
            result.scheduled.add(new ScheduledJob(job, elapsed));
            if (elapsed > job.deadline)
                result.missed.add(job);
        }
        result.used = elapsed;
        return result;
    }

    static void printResult(Result result) {
        System.out.println("\n--- " + result.algorithm + " ---");
        System.out.printf("%-10s %-12s %-12s %-12s %s%n",
                "Job ID", "Duration", "Deadline", "Priority", "Completion");

        for (ScheduledJob item : result.scheduled) {
            Job j = item.job;
            System.out.printf("%-10s %-12d %-12d %-12d %d",
                    j.id, j.duration, j.deadline, j.priority, item.completion);
            if (item.completion > j.deadline)
                System.out.print("  (LATE)");
            System.out.println();
        }

        System.out.print("Missed / rejected jobs: ");
        if (result.missed.isEmpty()) {
            System.out.print("None");
        } else {
            for (Job job : result.missed)
                System.out.print(job.id + " ");
        }

        double utilization = result.capacity == 0 ? 0.0
                : 100.0 * result.used / result.capacity;

        System.out.println("\nScheduled jobs: " + result.scheduled.size());
        System.out.println("Time used: " + result.used + " / "
                + result.capacity + " minutes");
        System.out.printf("Utilization: %.2f%%%n", utilization);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("HOSPITAL OPD / OPERATION SCHEDULER");
        System.out.print("Enter available working time in minutes: ");
        int capacity = sc.nextInt();

        System.out.print("Enter number of jobs/patients: ");
        int n = sc.nextInt();

        if (capacity <= 0 || n <= 0) {
            System.out.println("Please enter positive capacity and job count.");
            sc.close();
            return;
        }

        ArrayList<Job> jobs = new ArrayList<>();
        System.out.println("\nEnter each job as: ID Duration Deadline Priority");
        System.out.println("(Times are minutes from the start of the day.)");

        for (int i = 0; i < n; i++) {
            String id = sc.next();
            int duration = sc.nextInt();
            int deadline = sc.nextInt();
            int priority = sc.nextInt();

            if (duration <= 0 || deadline < 0) {
                System.out.println("Invalid duration or deadline. Run again.");
                sc.close();
                return;
            }
            jobs.add(new Job(id, duration, deadline, priority, i));
        }

        printResult(runGreedy(jobs, capacity));
        printResult(runBaseline(jobs, capacity, "FCFS"));
        printResult(runBaseline(jobs, capacity, "EDF"));

        System.out.println("\nNote: Greedy is a priority-based heuristic for");
        System.out.println("variable-duration jobs, not a guaranteed global optimum.");
        System.out.println("Clinical urgency must override administrative priority.");
        sc.close();
    }
}
