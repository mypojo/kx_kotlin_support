package net.kotlinx.core1.string

object CharUtil {

    //    /**
    //     * 자바는 유니코드를 사용함으로 모두 2byte로 처리하지만 다른 시스템 기준으로 바이트를 산정할때 사용한다.
    //     * 한글 1자 2byte 기타 1byte 로 계산 (UTF-8인 오라클은 3byte)
    //     * 일단 이거 사용하지 말고 MS949 캐릭터 변환으로 바이트 체크할것. -> 이게 안맞을때 아래 코드 사용
    //     */
    //    public static int getByteSizeWith2byte(String s) {
    //        int sumOfSize = 0;
    //        for (char c : s.toCharArray()) {
    //            sumOfSize += isHan(c) ? 2 : 1;
    //        }
    //        return sumOfSize;
    //    }
    //
    //    /**
    //     * 바이트를 초과하면 뒤에서부터 자른다.
    //     * 성능 같은거 신경안쓰고 만들었으니 주의
    //     * 그냥 바이트로 자르면 글자가 깨지기 때문에 이렇게 함.
    //     *  */
    //    public static String substringByByteSize(String text, Charset charset, int limit){
    //        Preconditions.checkArgument(limit > 0);
    //        Preconditions.checkArgument(limit <= 4000); //혹시나 해서
    //        if(Strings.isNullOrEmpty(text)) return "";
    //        if(text.length()*3 <= limit) return text;  //최대 3바이트 임으로 3배수 이하라면 무시
    //
    //        if(text.length() > limit) text = text.substring(0,limit); //일단 큰거 먼저 잘라줌
    //        while(true){
    //            int byteLength = text.getBytes(charset).length;
    //            if(byteLength <= limit) break;
    //            text = text.substring(0,text.length()-1);
    //        }
    //        return text;
    //    }
    //
    //    /** 최대값 이상이라면 최대값을 넘지 않도록 잘라준다. */
    //    public static String substringByLength(String param,int maxSize){
    //        if(param==null) return null;
    //        if(param.length() <= maxSize) return param;
    //        return param.substring(0,maxSize);
    //    }
    //
    //
    //    /**
    //     * 문자열을 캐릭터의 집합으로 변형시킨다.
    //     * 해당 단어와 동일한 문자열 구성원을 찾을때 사용했다.
    //     *  */
    //    public static Set<Character> toCharSet(String text){
    //        Set<Character> set = Sets.newHashSet();
    //        for(int i=0;i<text.length();i++){
    //            char w = text.charAt(i);
    //            set.add(w);
    //        }
    //        return set;
    //    }
    //
    //    /**
    //     * 문자열을 캐릭터의 집합으로 변형시킨다2.
    //     * 단어의 수까지 정확히 일치해야할때 사용한다.
    //     *  */
    //    public static Multiset<Character> toCharMultiSet(String text){
    //        Multiset<Character> set = HashMultiset.create();
    //        for(int i=0;i<text.length();i++){
    //            char w = text.charAt(i);
    //            set.add(w);
    //        }
    //        return set;
    //    }
    //
    //    /**
    //     * 엑셀 시트에서는 숫자를 알파벳으로 나타냄. 해당 기능을 위해서 생성
    //     * */
    //    public static String toXlsAlpha(int code){
    //        if(code > 26){
    //            return 'A'+ StringFormatUtil.intToUpperAlpha(code-26);
    //        } else {
    //            return StringFormatUtil.intToUpperAlpha(code); //A = 1 이다s
    //        }
    //    }
}