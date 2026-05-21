package ceb.domain.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminSearchItemResponse {

    private String type;
    private String id;
    private String title;
    private String subtitle;
    private String status;
    private String targetUrl;
    private double score;
}
