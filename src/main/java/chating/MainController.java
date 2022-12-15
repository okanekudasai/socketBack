package chating;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@CrossOrigin
public class MainController {
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
		System.out.println(roomNumber);
		return roomNumber;
	}
	
	//채팅룸 넘버를 가지고 채팅룸 인스턴스를 찾아요
	
	
	@MessageMapping("/socket/roomList")
	@SendTo("/topic/roomList")
	public void roomList() {
		System.out.println("룸리스트를 업데이트해요");
	}
}
