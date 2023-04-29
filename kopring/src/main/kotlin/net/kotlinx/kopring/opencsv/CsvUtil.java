//package net.kotlinx.kopring.opencsv;

//
//import java.io.*;
//import java.util.List;
//import java.util.zip.GZIPOutputStream;
//
//public abstract class CsvUtil {
//
//    public static String[] parse(String line){
//        return StringUtil.splitPreserveAllTokens(line, ',');
//    }
//
//    /**
//     * 간단 인메모리 Gzip 생성기
//     * try - finally 귀찮아서 분리함
//     *  */
//    public static void writeGzip(File outFile, List<String[]> lines){
//        GZIPOutputStream outputStream = null;
//        try {
//            outputStream = new GZIPOutputStream(new FileOutputStream(outFile));
//            OutputStreamResource res = OutputStreamResource.of(outputStream);
//            CsvItemWriter.<String[]>of(res).utf8().open().writeAndClose(lines);
//        } catch (FileNotFoundException e) {
//            throw ExceptionUtil.toRuntimeException(e);
//        } catch (IOException e) {
//            throw ExceptionUtil.toRuntimeException(e);
//        } finally {
//            IOUtil.closeSilently(outputStream);
//        }
//    }
//
//    /** S3 결과 등을 간단히 읽는 간이도구 */
//    public static void read(InputStream inputStream, ItemWriter<String[]> itemWriter){
//        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
//        CsvItemReader<String[]> csvItemReader = CsvItemReader.<String[]>of(inputStreamResource).utf8();
//
//        BatchExecutor executor = new BatchExecutor();
//        executor.setItemReader(csvItemReader);
//        executor.setItemWriter(itemWriter);
//        executor.execute();
//    }
//
//    /** S3 결과 등을 간단히 읽는 간이도구 - 인메모리 처리기 */
//    public static List<String[]> readAll(InputStream inputStream){
//        ListItemWriter<String[]> itemWriter = new ListItemWriter<>();
//        read(inputStream,itemWriter);
//        return (List<String[]>) itemWriter.getWrittenItems();
//    }
//
//    /** S3 결과 등을 간단히 읽는 간이도구 - 카운트 집계 */
//    public static long readCount(InputStream inputStream){
//        MutableLong counter = new MutableLong();
//        read(inputStream,items-> counter.add(items.size()));
//        return counter.longValue();
//    }
//
//
//}
