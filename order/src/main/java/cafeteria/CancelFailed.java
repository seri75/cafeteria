package cafeteria;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class CancelFailed extends AbstractEvent {

    @Getter @Setter
    private Long orderId;

}
