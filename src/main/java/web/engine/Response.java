package web.engine;

public class Response {
	private boolean success;
	private Object result;
	
	public Response(boolean success, Object result) {
		super();
		this.success = success;
		this.result = result;
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	
}
