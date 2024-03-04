package hello.advanced.trace.logtrace;

import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FieldLogTrace implements LogTrace{

    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    private TraceId traceIdHolder; // traceId 동기화, 동시성 이슈 발생

    @Override
    public TraceStatus begin(String message) {
        syncTraceId();
        TraceId traceId = traceIdHolder;
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX,
                traceId.getLevel()), message);
        return new TraceStatus(traceId, startTimeMs, message);
    }

    @Override
    public void end(TraceStatus status) {
        complete(status, null);
    }

    @Override
    public void exception(TraceStatus status, Exception e) {
        complete(status, e);
    }

    private void complete(TraceStatus status, Exception e){
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        TraceId traceId =  status.getTraceId();
        if (e == null) {
            log.info("[{}] {}{} time={}ms", traceId.getId(),
                    addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(),
                    resultTimeMs);
        } else {
            log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
                    addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs,
                    e.toString());
        }
        releaseTraceId();

    }

    /*
    - TraceId 를 새로 만들거나 앞선 로그의 TraceId를 참고해서 동기화하고, level도 장가한다.
    - 최초 호출이면 TraceId를 새로 만든다.
    - 직전 로그가 있으면 해당 로그의 TraceId를 참고해서 동기화 하고, level 도 증가한다.
    - 결과를 traceHolder 에 보관한다.
     */
    private void syncTraceId() {
        if(traceIdHolder == null){
            traceIdHolder = new TraceId();
        }else{
            traceIdHolder = traceIdHolder.createNextId();
        }
    }

    /*
     - 메서드를 추가로 호출할 때는 level 이 하나 증가해야 하지만, 메서드 로출이 끝나면 level 이 하나 감소해야 한다.
     - releaseTraceId() 는 level 을 하나 감소한다.
     - 만약 최초 호출 (level == 0) 이면 내부에서 관리하는 traceId 를 제거한다.
     */
    private void releaseTraceId(){
        if(traceIdHolder.isFirstLevel()){
            traceIdHolder = null;
        }else{
            traceIdHolder = traceIdHolder.createPreviousId();
        }
    }

    private static String addSpace(String prefix, int level){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < level; i++){
            sb.append((i == level - 1) ? "|" + prefix : "|  ");
        }
        return sb.toString();
    }
}
