package helpers;

import dal.DataAccessLayer;
import model.Member;
import model.Video;

public class SolutionInitiator {
	@SuppressWarnings("unchecked")
	public static IDalWrapper getWrapper(String taskName)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> dalClass = Class.forName("dal.impl." + taskName + "Dal");
		if ("Video".equals(taskName)) {
			return new DalWrapper<Video, Member>((DataAccessLayer<Video, Member>) dalClass.newInstance(), Video.class,
					Member.class, getFields(taskName));
		} else {
			throw new ClassNotFoundException();
		}
	}

	public static String[] getFields(String taskName) {
		if ("Video".equals(taskName)) {
			return new String[] { "videoId", "title", "director" };
		} else {
			return null;
		}
	}
}
