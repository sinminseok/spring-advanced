package hello.advanced.trace.template;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.logtrace.LogTrace;


/*
AbstractTemplate 는 템플릿 메서드 패턴에서 부모 클래스이고, 템플릿 역할을 한다.
<T> 제네릭을 사용했다. 반환 타입을 정의한다.
객체를 생성할때 내부에서 사용할 LogTrace trace 를 주입 받는다.
로그에 출력할 message를 외부에서 파라미터로 전달받는다.
템플릿 코드 중간에 call() 메서드를 통해서 변하는 부분을 처리한다.
abstract T call()은 변하는 부분을 처리하는 메서드이다. 이 부분은 상속으로 구현해야 한다.
 */
public abstract class AbstractTemplate<T> {

    private final LogTrace trace;

    protected AbstractTemplate(LogTrace trace) {
        this.trace = trace;
    }

    public T execute(String message) {
        TraceStatus status = null;
        try {
            status = trace.begin(message);

            //로직 호출
            T result = call();

            trace.end(status);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected abstract T call();
}
