//package net.kotlinx.hibernate.qdsl;
//
///**
// * QDSL 도우미
// * 있으면 조건절에 넣고
// * 없으면 무시한다.
// *
// * 필요한거 더 추가하세요.
// * or 연결은 ExpressionUtils 로 사용할것
// *
// * LIKE 이스케이핑 정책은 일단 제거. 필요시 '\' 로 이스케이핑 할것
// *
// * 너무 오래전에 만들었던거라 기억이 안나요... 코드 유실됨. 필요할때마다 추가해서 쓰세요.
// *
// * */
//public class QHelper {
//
//    private final BeanVo bean;
//
//    public QHelper(Object entity) {
//        this.bean = new BeanVo(entity);
//    }
//
//    //==================================================== string ======================================================
//
//    public BooleanExpression eq(StringPath path){
//        String value = bean.get(path.getMetadata().getName(),"");
//        if (StringUtil.isEmpty(value)) return null;
//        return path.eq(value);
//    }
//
//    public BooleanExpression like(StringPath path){
//        String value = bean.get(path.getMetadata().getName(),"");
//        String replaced = RegEx.SQL_LIKE.removeFrom(value); //SQL은 반드시 이스케이핑 해줘야 함
//        if (StringUtil.isEmpty(replaced)) return null;
//        return path.like("%" + replaced + "%");
//    }
//    public BooleanExpression likeStartsWith(StringPath path){
//        String value = bean.get(path.getMetadata().getName(),"");
//        String replaced = RegEx.SQL_LIKE.removeFrom(value); //SQL은 반드시 이스케이핑 해줘야 함
//        if (StringUtil.isEmpty(replaced)) return null;
//        return path.like( replaced+"%");
//    }
//    public BooleanExpression likeEndsWith(StringPath path){
//        String value = bean.get(path.getMetadata().getName(),"");
//        String replaced = RegEx.SQL_LIKE.removeFrom(value); //SQL은 반드시 이스케이핑 해줘야 함
//        if (StringUtil.isEmpty(replaced)) return null;
//        return path.like("%" + replaced);
//    }
//
//    //==================================================== long ======================================================
//
//    public  BooleanExpression eq(NumberPath<Long> path){
//        Long value = bean.get(path.getMetadata().getName(),null);
//        if (value==null) return null;
//        return path.eq(value);
//    }
//    //==================================================== enum ======================================================
//
//    public <T extends Enum<T>> BooleanExpression eq(EnumPath<T> path){
//        T value = bean.get(path.getMetadata().getName(),null);
//        if (value==null) return null;
//        return path.eq(value);
//    }
//
//    public <T extends Enum<T>> BooleanExpression in(EnumPath<T> path){
//        T value = bean.get(path.getMetadata().getName(),null);
//        if (value==null) return null;
//        return path.in(value);
//    }
//
//    //==================================================== int (잘 안쓰니 이걸 리네이밍) ======================================================
//
//    public  BooleanExpression eqInt(NumberPath<Integer> path){
//        Integer value = bean.get(path.getMetadata().getName(),null);
//        if (value==null) return null;
//        return path.eq(value);
//    }
//
//    public BooleanExpression betweenInt(NumberPath<Integer> from,NumberPath<Integer> to){
//        Integer fromValue = bean.get(from.getMetadata().getName(),null);
//        Integer toValue = bean.get(to.getMetadata().getName(),null);
//        if (fromValue==null || toValue ==null) return null;
//        return from.between(fromValue,toValue);
//    }
//
//
//}
