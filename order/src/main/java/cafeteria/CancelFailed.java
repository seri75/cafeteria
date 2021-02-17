package cafeteria;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString @Getter @Setter
public class CancelFailed extends AbstractEvent {

	
    private Long orderId;
	
    private String phoneNumber;

}
