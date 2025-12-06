package net.kotlinx.notion

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.json.gson.json

/**
 * PRD 초기 본문(노션 블록 children)을 생성하는 헬퍼
 */
fun prdInitialContents(): Any = json {
    "children" to arr[
        // heading_3 : 목적/배경
        obj {
            "object" to "block"
            "type" to "heading_3"
            "heading_3" to obj {
                "rich_text" to arr[
                    obj {
                        "type" to "text"
                        "text" to obj { "content" to "목적/배경" }
                    }
                ]
            }
        },
        // numbered_list_item 들
        obj {
            "object" to "block"
            "type" to "numbered_list_item"
            "numbered_list_item" to obj {
                "rich_text" to arr[
                    obj {
                        "type" to "text"
                        "text" to obj { "content" to "NHN 보안팀의 요청으로 사내 솔루션 전체에 대해서,  내부 인원만 접속 가능하도록 보안 조치가 필요함" }
                    }
                ]
            }
        },
        obj {
            "object" to "block"
            "type" to "numbered_list_item"
            "numbered_list_item" to obj {
                "rich_text" to arr[
                    obj {
                        "type" to "text"
                        "text" to obj { "content" to "기존 시스템에는 이러한 조치가 되어있지 않음." }
                    }
                ]
            }
        },
        obj {
            "object" to "block"
            "type" to "numbered_list_item"
            "numbered_list_item" to obj {
                "rich_text" to arr[
                    obj {
                        "type" to "text"
                        "text" to obj { "content" to "12월 중순까지 개발 및 테스트가 완료되어야함 (약 2주 남음)" }
                    }
                ]
            }
        },

        // heading_3 : 대상 프로젝트 & 메뉴
        obj {
            "object" to "block"
            "type" to "heading_3"
            "heading_3" to obj {
                "rich_text" to arr[
                    obj {
                        "type" to "text"
                        "text" to obj { "content" to "대상 프로젝트 & 메뉴" }
                    }
                ]
            }
        },
        // paragraph : 각 라인을 개별 블록으로 추가
        obj {
            "object" to "block"
            "type" to "paragraph"
            "paragraph" to obj {
                "rich_text" to arr[
                    obj { "type" to "text"; "text" to obj { "content" to "dmp" } }
                ]
            }
        },
        obj {
            "object" to "block"
            "type" to "paragraph"
            "paragraph" to obj {
                "rich_text" to arr[
                    obj { "type" to "text"; "text" to obj { "content" to "nhnad" } }
                ]
            }
        },
        // heading_3 : 요구사항 및 사유
        obj {
            "object" to "block"
            "type" to "heading_3"
            "heading_3" to obj {
                "rich_text" to arr[
                    obj { "type" to "text"; "text" to obj { "content" to "요구사항 및 사유" } }
                ]
            }
        },
        obj {
            "object" to "block"
            "type" to "numbered_list_item"
            "numbered_list_item" to obj {
                "rich_text" to arr[
                    obj { "type" to "text"; "text" to obj { "content" to "모든 내부 프로젝트를 close 한 뒤 특정 IP만 접근 가능하도록 재설계 되어야함" } }
                ]
            }
        },

        // heading_3 : 개발상세기능
        obj {
            "object" to "block"
            "type" to "heading_3"
            "heading_3" to obj {
                "rich_text" to arr[
                    obj { "type" to "text"; "text" to obj { "content" to "개발상세기능" } }
                ]
            }
        },
        // table : 4열, 헤더 포함 + 본문 2행 예시
        obj {
            "object" to "block"
            "type" to "table"
            "table" to obj {
                "table_width" to 4
                "has_column_header" to true
                "has_row_header" to false
                "children" to arr[
                    obj {
                        "object" to "block"
                        "type" to "table_row"
                        "table_row" to obj {
                            "cells" to arr[
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "내용" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "예상공수" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "설명" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "실공수" } }
                                ],
                            ]
                        }
                    },
                    // 본문 1행
                    obj {
                        "object" to "block"
                        "type" to "table_row"
                        "table_row" to obj {
                            "cells" to arr[
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "항목1" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "1" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "설명 예시" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "-" } }
                                ],
                            ]
                        }
                    },
                    // 본문 2행
                    obj {
                        "object" to "block"
                        "type" to "table_row"
                        "table_row" to obj {
                            "cells" to arr[
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "항목2" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "14d" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "추가 설명" } }
                                ],
                                arr[
                                    obj { "type" to "text"; "text" to obj { "content" to "-" } }
                                ],
                            ]
                        }
                    }
                ]
            }
        }
    ]
}
