/** Prevent requests from an old identity from updating the next session. */
export class SessionScope {
  private version = 0;
  capture() {
    return this.version;
  }
  invalidate() {
    this.version += 1;
    return this.version;
  }
  isCurrent(version: number) {
    return this.version === version;
  }
  assertCurrent(version: number) {
    if (!this.isCurrent(version))
      throw new Error("Your session changed. Please try again.");
  }
}
