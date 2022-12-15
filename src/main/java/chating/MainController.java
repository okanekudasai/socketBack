package chating;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin
public class MainController {

	@Autowired
	SimpMessagingTemplate messagingTemplate;
	
	public static LinkedList<ChatingRoom> chatingRoomList = new LinkedList<>();
	
	//채팅 룸리스트를 반환해요
	@GetMapping("/chatingRoomList")
	@ResponseBody
	public LinkedList<ChatingRoom> chatingRoomList() {
		return chatingRoomList;
	}
	
	//채팅룸을 만들어요
	@PostMapping("/chatingRoom")
	@ResponseBody
	public String makeRoom(@RequestBody HashMap<String, String> map) {
		String roomNumber = UUID.randomUUID().toString();
		ChatingRoom chatingRoom = ChatingRoom.builder()
				.roomNumber(roomNumber)
				.users(new LinkedList<>())
				.roomName(map.get("roomName"))
				.build();
		chatingRoom.getUsers().add(map.get("userId"));
		chatingRoomList.add(chatingRoom);
		return roomNumber;
	}
	
	//채팅룸 넘버를 가지고 채팅룸 인스턴스를 찾아요
	@PostMapping("/findRoom")
	@ResponseBody
	public ChatingRoom findRoomByNumber(@RequestBody String roomNumber) {
		ChatingRoom room = ChatingRoom.builder().roomNumber(roomNumber).build(); 
		int index = chatingRoomList.indexOf(room);
		
		if(chatingRoomList.contains(room)) {
			return chatingRoomList.get(index); 
		}
		return null;
	}
	
	//채팅방에 들어가요
	@PostMapping("/enterChat")
	@ResponseBody
	public String enterChat(@RequestBody HashMap<String, String> map) {
		ChatingRoom room = ChatingRoom.builder().roomNumber(map.get("roomNumber")).build(); 
		int index = chatingRoomList.indexOf(room);
		
		ChatingRoom thisRoom = chatingRoomList.get(index);
		thisRoom.getUsers().add(map.get("userId"));
		
		return thisRoom.getRoomName();
	}
	
	//채팅방에서 나가요
	@PostMapping("/outChat")
	@ResponseBody
	public void outChat(@RequestBody HashMap<String, String> map) {
		ChatingRoom room = ChatingRoom.builder().roomNumber(map.get("roomNumber")).build(); 
		int index = chatingRoomList.indexOf(room);
		
		ChatingRoom thisRoom = chatingRoomList.get(index);
		thisRoom.getUsers().remove(map.get("userId"));
	}
	
	@MessageMapping("/socket/roomList")
	@SendTo("/topic/roomList")
	public String roomList() {
//		룸리스트를 업데이트해요
//		리턴을 null하거나 void함수로 만들면 subscribe가 아무것도 잡지 못하는거 같아요
		return "";
	}
	
	// 채팅방에서 메세지 보내기
	@MessageMapping("/socket/sendMessage/{roomNumber}")
	@SendTo("/topic/message/{roomNumber}")
	public Message sendMessage(@DestinationVariable String roomNumber, Message mapa) {
//		System.out.println(roomNumber);
//		System.out.println(mapa);
		return mapa;
	}
	
	// 채팅방에 입장 퇴장 메세지 보내기
	@MessageMapping("/socket/notification/{roomNumber}")
	@SendTo("/topic/notification/{roomNumber}")
	public LinkedList<String> notification(@DestinationVariable String roomNumber) {
//		System.out.println(roomNumber);
		ChatingRoom room = ChatingRoom.builder().roomNumber(roomNumber).build(); 
		int index = chatingRoomList.indexOf(room);
		
		ChatingRoom thisRoom = chatingRoomList.get(index);
//		System.out.println(thisRoom.getUsers());
//		System.out.println("남은 사람수 :" + thisRoom.getUsers().size());
		if (thisRoom.getUsers().size() == 0) {
			chatingRoomList.remove(thisRoom);
			//convertAndSendToUser라는 메서드도 있는데 그건 sessionId를 가지고 보내는 거에요. 그래서 특정유저에게만 보낼 수 있죠
			//잘모르지만 sessionId는 인터셉터에서 받을 수 있는거 같아요 그걸 서버에 저장해 두는 거죠 그럼 이 구독 지옥에서 벗어날 수 있을 거에요
			messagingTemplate.convertAndSend("/topic/roomList", "");
		}
		return thisRoom.getUsers();
	}
}