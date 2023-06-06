package MOSprite;

public interface SpriteSourceInterface {
	Sprite getSpriteInstance(boolean setRandomStreamKeyPositionWithID);
	public Sprite getSpriteInstance(SpriteSeed s, boolean setRandomStreamKeyPositionWithID);
	void setRandomStreamKeyPosition(int keyPos);
}
