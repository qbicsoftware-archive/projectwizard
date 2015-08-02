package model;

public class TestSampleInformation {

  private String technology;
  private boolean pool;
  private int replicates;

  public TestSampleInformation(String tech, boolean pool, int reps) {
    this.replicates = reps;
    this.pool = pool;
    this.technology = tech;
  }

  public String getTechnology() {
    return technology;
  }

  public boolean isPooled() {
    return pool;
  }

  public int getReplicates() {
    return replicates;
  }

}
