package ceb.domain.res;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {

    private List<ProductResponse> cayCanh;
    private List<ProductResponse> chauCay;
    private List<ProductResponse> phuKien;
}
