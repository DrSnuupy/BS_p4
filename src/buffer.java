class buffer
{
    private boolean available = false;
	private int data;

	public synchronized void put(int x) {
		while(available) {
			try {
				wait();
			}
			catch(InterruptedException e) {}
		}
		data = x;
		available = true;
		notify();
	}

	public synchronized int get() {
		while(!available) {
			try {
				wait();
			}
			catch(InterruptedException e) {}
		}
		available = false;
		notify();
		return data;
	}
}