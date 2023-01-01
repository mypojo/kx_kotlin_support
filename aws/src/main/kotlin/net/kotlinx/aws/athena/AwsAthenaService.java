//package net.kotlinx.aws.module;
//
//import com.epe.aws2.s3.AwsS3Client;
//import com.epe.aws2.s3.AwsS3Data;
//import com.epe.opencsv.CsvItemReader;
//import com.epe.opencsv.CsvItemWriter;
//import com.epe.spring.batch.BatchExecutor;
//import com.epe.spring.batch.component.MultiResourceItemWriterBuilder;
//import com.epe.util.concurrent.ThreadSleepTool;
//import com.epe.util.file.FileManageUtil;
//import com.epe.util.file.FileZipUtil;
//import com.epe.util.guava.gson.GsonData;
//import com.epe.util.jdbc.JdbcBatch;
//import com.epe.util.jdbc.JdbcBatchItemWriter;
//import com.epe.util.lib.CollectionUtil;
//import com.epe.util.text.format.StringFormatUtil;
//import com.epe.util.time.TimeFormat;
//import com.epe.util.time.TimeString;
//import com.epe.util.time.TimeUtil;
//import com.google.common.base.CaseFormat;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//import lombok.experimental.Accessors;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.item.ExecutionContext;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.file.MultiResourceItemWriter;
//import org.springframework.core.io.InputStreamResource;
//import software.amazon.awssdk.core.ResponseInputStream;
//import software.amazon.awssdk.services.athena.model.QueryExecution;
//import software.amazon.awssdk.services.athena.model.QueryExecutionState;
//import software.amazon.awssdk.services.s3.model.GetObjectResponse;
//
//import java.io.File;
//import java.time.LocalDate;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Consumer;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Athena로 배치작업을 쉽게하기 위한 도우미
// *
// * 1. 원본 client는 final 객체라 프록시가 안먹는다. 이때문에 별도 client로 빼고 여기서 리트라이를 적용시킨다.
// * 2. 각종 편의도구를 결합해서 제공
// *
// *
// * ex) AwsAthenaService athenaService = AwsAthenaService.builder().athenaClient(AwsAthenaUtil.LIMIT_RETRY_CONFIG.proxy(client)).build();
// * */
//@Slf4j
//@Accessors(fluent = true)
//@RequiredArgsConstructor
//public class AwsAthenaService {
//
//    //==================================================== queryAndWait ======================================================
//
//    /**
//     * 한번에 2개를 호출
//     * 쿼리 결과는 필요 없는것들
//     * ex) MSCK REPAIR TABLE kwd_seed_csv
//     *  */
//    public QueryExecution queryAndWait(String query) {
//        String queryExecutionId = athenaClient.query(query);
//        QueryExecution queryExecution = waitForQuery(queryExecutionId);
//        if(checkThrow) AthenaUtil.checkAndThrow(queryExecution);
//        return queryExecution;
//    }
//
//    /**
//     * 한번에 다 처라힌다. 5~10개 이상을 전달하는경우  유리함. 5초 이상 줄것
//     * @param checkIntervalSec 대량이라서 별도의 체크기간을 둔다
//     * */
//    public List<QueryExecution> queryAndWaits(Collection<String> querys, int checkIntervalSec) {
//        ThreadSleepTool sleepTool = ThreadSleepTool.interval(TimeUnit.MILLISECONDS, batchSleepTime);
//        List<String> queryExecutionIds = querys.stream().map(query -> {
//            sleepTool.checkAndSleepWithoutException();
//            return athenaClient.query(query);
//        }).collect(Collectors.toList());
//        List<QueryExecution> queryExecutions = waitForQuerys(queryExecutionIds, checkIntervalSec);
//        if(checkThrow) AthenaUtil.checkAndThrow(queryExecutions);
//        return queryExecutions;
//    }
//
//    //==================================================== queryAndWaitResult ======================================================
//
//    /**
//     * 청크 단위별 처리용도 (대용량)
//     * ex) awsAthenaClient.queryAndWaitResult(queryString, AwsAthenaUtil.buildEntryConsumer(limit, kwds));
//     *  */
//    public QueryExecution queryAndWaitResult(String query, Consumer<Map.Entry<List<String>,List<List<String>>>> consumer){
//        QueryExecution queryExecution = queryAndWait(query);
//        athenaClient.load(queryExecution.queryExecutionId(),consumer);
//        return queryExecution;
//    }
//
//    /** 인메모리 처리용도 (소용량) */
//    public AwsAthenaResult queryAndWaitResult(String query){
//        AwsAthenaResult result = new AwsAthenaResult();
//        QueryExecution queryExecution = queryAndWaitResult(query, result);
//        result.setQueryExecution(queryExecution);
//        return result;
//    }
//
//    //==================================================== waitForQuery ======================================================
//
//    /**
//     * 쿼리 끝날때까지 대기.
//     * !!! 경고!!! 실무에서는 이렇게 쓰면 안됨! 병렬 처리할것
//     *  */
//    public QueryExecution waitForQuery(String queryExecutionId)  {
//        ThreadSleepTool sleep = ThreadSleepTool.interval(TimeUnit.SECONDS, checkInterval);
//        for (int i = 0; i < 1000; i++) {
//            sleep.checkAndSleepWithoutException();
//            QueryExecution queryExecution = athenaClient.queryState(queryExecutionId);
//            QueryExecutionState state = queryExecution.status().state();
//            if(AthenaUtil.isCompleted(state)) return queryExecution;
//            log.debug("  ==> [{}] : {} ...",queryExecutionId,state);
//        }
//        athenaClient.stopQueryExecution(queryExecutionId);
//        throw new IllegalStateException("너무 오래 기다려서 중단시킵니다. 해당 쿼리는 강제 중지함.");
//    }
//
//    /** 50개의 제한이 있다. 어차피 전부 체크해야 메인 스래드가 풀리니 따로 병렬처리 하지않음  */
//    private List<QueryExecution> waitForQuerys(List<String> queryExecutionIds, int checkIntervalSec) {
//        List<List<String>> splited = CollectionUtil.splitBySize(queryExecutionIds, API_LIMIT_SIZE);
//        return splited.stream().flatMap(qeIds -> waitForQuerysInternal(qeIds, checkIntervalSec).stream() ).collect(Collectors.toList());
//    }
//
//    private List<QueryExecution> waitForQuerysInternal(List<String> queryExecutionIds, int checkIntervalSec) {
//        ThreadSleepTool sleep = ThreadSleepTool.interval(TimeUnit.SECONDS, checkIntervalSec);
//        for (int i = 0; i < 1000; i++) {
//            sleep.checkAndSleepWithoutException();
//            List<QueryExecution> queryExecutions = athenaClient.queryStates(queryExecutionIds);
//
//            long completedCnt = queryExecutions.stream().filter(v -> AthenaUtil.isCompleted(v.status().state())).count();
//            log.debug("  ==> 완료상태 {} / {}",completedCnt, queryExecutionIds.size());
//
//            if(completedCnt == queryExecutionIds.size()) return queryExecutions;
//        }
//        //벌크는 별도의 스탑 안해줌
//        throw new IllegalStateException("너무 오래 기다려서 중단시킵니다.");
//    }
//
//
//    //==================================================== 기본기능 ======================================================
//
//    /** 이걸 베이스로 동작 */
//    public int queryAndS3Download(String sql, ItemWriter<String[]> itemWriter) {
//        return queryAndS3Download(sql, itemWriter, 1);
//    }
//
//    public int queryAndS3Download(String sql, ItemWriter<String[]> itemWriter, int linesToSkip) {
//
//        CsvItemReader<String[]> itemReader = queryAndGetItemReader(sql, linesToSkip);
//
//        BatchExecutor mock = new BatchExecutor();
//        mock.setItemReader(itemReader);
//        mock.setItemWriter(itemWriter);
//
//        ExecutionContext ex = mock.execute();
//        int readCnt = ex.getInt("read.count") - 1; //헤더 제외
//        return readCnt;
//    }
//
//    /** 쿼리 실행후 리더를 가져옴 */
//    public CsvItemReader<String[]> queryAndGetItemReader(String sql, int linesToSkip) {
//        QueryExecution qe = queryAndWait(sql);
//        String outputLocation = qe.resultConfiguration().outputLocation();
//
//        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(AwsS3Data.parse(outputLocation));
//
//        CsvItemReader<String[]> itemReader = new CsvItemReader<String[]>().utf8().resource(new InputStreamResource(s3Object)).linesToSkip(linesToSkip); ////첫 로우는 헤더이다.
//        return itemReader;
//    }
//
//    /** 이걸 베이스로 동작 */
//    public <T> int queryAndS3ReadAndWrite(String sql,Class<T> clazz, ItemWriter<T> itemWriter) {
//
//        ItemWriter<String[]> innerWriter = new ItemWriter<>() {
//
//            private String[] header;
//
//            @Override
//            public void write(List<? extends String[]> items) throws Exception {
//                if (items.isEmpty()) return;
//
//                //첫 행은 무조건 헤더이다.
//                long skip = 0L;
//                if (header == null) {
//                    String[] queryHeader = CollectionUtil.getFirst(items);
//                    header = Stream.of(queryHeader).map(v -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,v)).toArray(String[]::new);
//                    skip++;
//                }
//                List<T> collect = items.stream().skip(skip).map(line -> GsonData.object().putAll(header, line).fromJson(clazz)).collect(Collectors.toList());
//                itemWriter.write(collect);
//            }
//        };
//
//        return queryAndS3Download(sql, innerWriter, 0);
//    }
//
//    //==================================================== 이하 응용 ======================================================
//
//    /**
//     * 테이블 파티션을 삭제한다. S3도 같이 지워줄것
//     * 보통 하루치씩 돌기때문에 일단 최적화는 안함
//     *  */
//    public void repartitionByBasicDate(String tableName,int day){
//        TimeString start = new TimeString();
//        assertThat(day).isGreaterThanOrEqualTo(7);
//        LocalDate today = LocalDate.now();
//        List<String> validDays = TimeUtil.toList(today.minusDays(day - 1), today).stream().map(v -> TimeFormat.YMD.get(v)).collect(Collectors.toList());
//
//        List<String> existBasicDates = queryAndWaitResult(StringFormatUtil.format("SELECT distinct basic_date FROM \"{}$partitions\"", tableName)).getLines().stream().map(v -> v.get(0)).collect(Collectors.toList());
//        log.info("테이블 [{}] : 파티션 유효기간 : {} ~ {} => 전체 날짜 파티션 수 : {}",tableName,CollectionUtil.getFirst(validDays),CollectionUtil.getLast(validDays),existBasicDates.size());
//
//        existBasicDates.removeAll(validDays);
//        Collections.sort(existBasicDates);
//        for (String existBasicDate : existBasicDates) {
//            log.debug(" -> 테이블 [{}] : 파티션 삭제 -> {}",tableName,existBasicDate);
//            queryAndWait(StringFormatUtil.format("ALTER TABLE {} DROP PARTITION (basic_date = {})",tableName,existBasicDate));
//        }
//        log.info("테이블 [{}] : 파티션 {}건 삭제완료 : {}",tableName,existBasicDates.size(),start);
//    }
//
//
//    /** athena 쿼리 -> DB로 입력 */
//    public int insertInto(JdbcBatch jdbcBatch,String sql, String tableName){
//        JdbcBatchItemWriter itemWriter = new JdbcBatchItemWriter(jdbcBatch,tableName);
//        return queryAndS3Download(sql, (ItemWriter)itemWriter); //그냥 쓴다.
//    }
//
//    /**
//     * athena 쿼리 -> CSV ZIP으로 다운로드
//     * @param fileName abc.csv
//     * */
//    public File splitDownloadAndZip(String sql, File outDir,String fileName,String[] header){
//
//        CsvItemWriter<String[]> delegator = new CsvItemWriter<>();
//        delegator.header(header);
//
//        MultiResourceItemWriterBuilder writerBuilder = new MultiResourceItemWriterBuilder(delegator, outDir, fileName);
//        MultiResourceItemWriter<String[]> itemWriter = writerBuilder.build();
//        int cnt = queryAndS3Download(sql, itemWriter);
//        log.debug(" -> 파일 {}건 다운로드",cnt);
//
//        File zipFile = FileZipUtil.zipDirectory(outDir);
//        FileManageUtil.deleteRecursively(outDir);
//        return zipFile;
//    }
//
//
//}