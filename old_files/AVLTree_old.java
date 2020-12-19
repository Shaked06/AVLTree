/**
*
* AVLTree
*
* An implementation of a AVL Tree with
* distinct integer keys and info
*
*/


public class AVLTree_old {
	//I set the initial root as an
	// external leaf with no parent because of join func

	private final ExtLeaf extLeaf = new ExtLeaf();
	private IAVLNode root=extLeaf;

	/**
	 * public boolean empty()
	 * <p>
	 * returns true if and only if the tree is empty
	 */
	//if the root is an external leaf it's an empty tree
	public boolean empty() {
		return root.getHeight() == -1;
	}

	/**
	 * public String search(int k)
	 * <p>
	 * returns the info of an item with key k if it exists in the tree
	 * otherwise, returns null
	 */
	//according to the pseudo code in BST page 16
	public String search(int k) {
		if (this.empty()) {
			return null;
		}//return null in case the tree is empty

		IAVLNode node = root;
		while (node.getHeight() != -1) {
			if (node.getKey() == k) {
				return node.getValue();
			} else if (node.getKey() > k) {
				node = node.getLeft();
			} else {
				node = node.getRight();
			}
		}
		if (node.getValue() == null) {
			return null;
		} //return null in case k isn't in the tree
		return node.getValue();  // to be replaced by student code
	}

	/**
	 * public int insert(int k, String i)
	 * <p>
	 * inserts an item with key k and info i to the AVL tree.
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were necessary.
	 * promotion/rotation - counted as one rebalnce operation, double-rotation is counted as 2.
	 * returns -1 if an item with key k already exists in the tree.
	 */
	//the beginning is from BST page 24

	public int insert(int k, String i) {
		AVLNode y = (AVLNode) TreePosition(root, k);

		AVLNode node = new AVLNode(k, i);   // create the new AVLNode for the new node

		if (y == null) {                   //if the tree is empty insert the node as the root and return 0
			node.setHeight(0);
			node.setLeft(this.extLeaf);
			node.setRight(this.extLeaf);
			this.root = node;
			return 0;
		}
//		System.out.println("y = "+y.getKey());
		if (k == y.getKey()) {            // if the node already exists return -1
			return -1;
		}

		node.setParent(y);                      // set node's parent to be y


		if (node.getKey() < y.getKey()) {       // insert node as left son
			y.setLeft(node);
		} else {                                // insert node a right son
			y.setRight(node);
		}
		node.setRight(this.extLeaf);           // setting external leaves
		node.setLeft(this.extLeaf);
		node.setHeight(0);                     // setting node height to 0

		// Rebalancing Process
		int steps = 0;  // if y isn't a leaf the tree is balanced so return 0
		if (y.getHeight() == 0) {    // if y is a leaf we need to rebalance the tree
			steps = balanceAfterInsert(y);
		}
		return steps;
	}

	/** function for rebalance after insertion  */
	public int balanceAfterInsert(IAVLNode y){
		int steps = 0;
		int y_left_rank_diff = ((AVLNode) y).rank_difference(y.getLeft());       // calc rank difference between node and sons
		int y_right_rank_diff = ((AVLNode) y).rank_difference(y.getRight());

		// while node is not 1-1, 1-2 or 2-1
		while (!((y_left_rank_diff == 1 && y_right_rank_diff == 1) || (y_left_rank_diff == 1 && y_right_rank_diff == 2)
				|| (y_left_rank_diff == 2 && y_right_rank_diff == 1))) {


			// for 0-1 or 1-0 node we need to promote y
			if ((y_left_rank_diff == 1 && y_right_rank_diff == 0) || (y_left_rank_diff == 0 && y_right_rank_diff == 1)) {
				y.setHeight(y.getHeight()+1);
				steps++;
				// if we are in the root, exit loop
				if(y.getParent() == null){ this.root = y; break;}
			}

			// for 0-2 node
			else {
				if (y_left_rank_diff == 0 && y_right_rank_diff == 2) {
					// calc rank differences for y left son and his sons to decided which rotate to do
					// current node: y.getLeft()
					int y_leftleft_rank_diff = ((AVLNode) y.getLeft()).rank_difference(y.getLeft().getLeft());
					int y_leftright_rank_diff = ((AVLNode) y.getLeft()).rank_difference(y.getLeft().getRight());

					// for 1-2 node we need to rotate right
					if (y_leftleft_rank_diff == 1 && y_leftright_rank_diff == 2) {
						IAVLNode curr = rotate_right(y, y.getLeft());

						curr.getRight().setHeight(curr.getRight().getHeight()-1);
						if(curr.getParent() != null){ fix_parent(curr, y.getKey());}
						else{ this.root = curr;}

						return steps+2;
					}
					// for 2-1 node we need double rotation: left-right rotation
					if (y_leftleft_rank_diff == 2 && y_leftright_rank_diff == 1) {
						IAVLNode curr = rotate_left_right(y, y.getLeft(), y.getLeft().getRight());

						curr.setHeight(curr.getHeight()+1);
						curr.getLeft().setHeight(curr.getLeft().getHeight()-1);
						curr.getRight().setHeight(curr.getRight().getHeight()-1);
						if(curr.getParent() != null){ fix_parent(curr, y.getKey());}
						else{ this.root = curr;}
						return steps+5;
					}
				}
				// for 2-0 nodes
				else {
					if (y_left_rank_diff == 2 && y_right_rank_diff == 0) {

						// calc rank differences for y right son and his sons to decided which rotate to do
						// current node: y.getLeft()
						int y_rightleft_rank_diff =  ((AVLNode) y.getRight()).rank_difference(y.getRight().getLeft());
						int y_rightright_rank_diff = ((AVLNode) y.getRight()).rank_difference(y.getRight().getRight());

						// for 1-2 node we need double rotation: right-left rotation
						if (y_rightleft_rank_diff == 1 && y_rightright_rank_diff == 2) {

							IAVLNode curr = rotate_right_left(y, y.getRight(), y.getRight().getLeft());

							curr.setHeight(curr.getHeight()+1);
							curr.getLeft().setHeight(curr.getLeft().getHeight()-1);
							curr.getRight().setHeight(curr.getRight().getHeight()-1);
							if(curr.getParent() != null){ fix_parent(curr, y.getKey());}
							else{ this.root = curr;}

							return steps+5;
						}

						// for 2-1 node we need one rotation: left rotation
						if (y_rightleft_rank_diff == 2 && y_rightright_rank_diff == 1) {
							System.out.println("dsds");
							IAVLNode curr = rotate_left(y, y.getRight());

							curr.getLeft().setHeight(curr.getLeft().getHeight()-1);
							if(curr.getParent() != null){ fix_parent(curr, y.getKey());}
							else{ this.root = curr;}

							return steps+2;
						}
					}
				}
			}

			y = (AVLNode) y.getParent();
			y_left_rank_diff = ((AVLNode) y).rank_difference(y.getLeft());   // calc rank difference between node and left son
			y_right_rank_diff = ((AVLNode) y).rank_difference(y.getRight()); // calc rank difference between node and right son

		}
		return steps;
	}
	/** function that connect the current node's parent to the current node, used after the tree is rotate */
	public void fix_parent(IAVLNode curr, int former_key){
		IAVLNode parent = curr.getParent();
		if (parent.getRight().getKey() == former_key){
			parent.setRight(curr);
		}
		else{
			parent.setLeft(curr);
		}

	}
	/** function for left rotation */
	public IAVLNode rotate_left(IAVLNode curr, IAVLNode right_son) {
		IAVLNode tmp = right_son.getLeft();
		right_son.setLeft(curr);
		right_son.setParent(curr.getParent());
		curr.setParent(right_son);
		curr.setRight(tmp);
		tmp.setParent(curr);
		return right_son;

	}
	/** function for right rotation */
	public IAVLNode rotate_right(IAVLNode curr, IAVLNode left_son) {
		IAVLNode tmp = left_son.getRight();
		left_son.setRight(curr);
		left_son.setParent(curr.getParent());
		curr.setParent(left_son);
		curr.setLeft(tmp);
		tmp.setParent(curr);
		return left_son;
	}
	/** function for right-left rotation */
	public IAVLNode rotate_right_left(IAVLNode curr, IAVLNode right_son, IAVLNode rightleft_son) {
		IAVLNode tmp_left = rightleft_son.getLeft();
		IAVLNode tmp_right = rightleft_son.getRight();

		rightleft_son.setParent(curr.getParent());
		curr.setParent(rightleft_son);
		rightleft_son.setLeft(curr);
		curr.setRight(tmp_left);
		tmp_left.setParent(curr);

		rightleft_son.setRight(right_son);
		right_son.setParent(rightleft_son);
		right_son.setLeft(tmp_right);
		tmp_right.setParent(right_son);
		return rightleft_son;

	}
	/** function for left-right rotation */
	public IAVLNode rotate_left_right(IAVLNode curr, IAVLNode left_son, IAVLNode leftright_son) {
		IAVLNode tmp_left = leftright_son.getLeft();
		IAVLNode tmp_right = leftright_son.getRight();

		leftright_son.setParent(curr.getParent());

		leftright_son.setLeft(left_son);
		left_son.setParent(leftright_son);
		leftright_son.setRight(curr);
		curr.setParent(leftright_son);

		left_son.setRight(tmp_left);
		tmp_left.setParent(left_son);
		curr.setLeft(tmp_right);
		tmp_right.setParent(curr);
		return leftright_son;

	}
	/**
	 * public int delete(int k)
	 * <p>
	 * deletes an item with key k from the binary tree, if it is there;
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were needed.
	 * demotion/rotation - counted as one rebalnce operation, double-rotation is counted as 2.
	 * returns -1 if an item with key k was not found in the tree.
	 */
	public int delete(int k) {
		if (search(k) == null) { return -1; } // if the key K is not in the tree - return -1
		if (k == this.root.getKey()){
			return -100;
		}
		IAVLNode node = TreePosition(root, k);  // node.key = k

		if (node.getHeight() == 0) {            //  if the node is a leaf
			if (isLeftSon(node)) {              // check if node is left son
				node.getParent().setLeft(this.extLeaf);
			}                                   // if the node is a right son
			else {
				node.getParent().setRight(this.extLeaf);
			}
		}
		else if (node.getLeft().getHeight() != -1 && node.getRight().getHeight() == -1) {  //  if node has only left son
			if (isLeftSon(node)) {                         // check if node is left son
				node.getParent().setLeft(node.getLeft());  // change node's parent left son to node's left son
			} else {
				node.getParent().setRight(node.getLeft());     // node is right son, change node's parent right son to node's left son
			}
		}
		else if (node.getLeft().getHeight() == -1 && node.getRight().getHeight() != -1) {  //if node has only right son
			if (isLeftSon(node)) {                         // check if node is left son
				node.getParent().setLeft(node.getRight()); // change node's parent left son to node's right son
			} else {                                         // node is right son, change node's parent right son to node's right son
				node.getParent().setRight(node.getRight());
			}
		}
		else {                                    // if the node has 2 sons - according to BST page 27
			IAVLNode s = successor(node);
			s.getParent().setRight(s.getRight());
			s.getRight().setParent(s.getParent());
			s.setRight(node.getRight());
			s.setLeft(node.getLeft());
			s.setParent(node.getParent());
			s.setHeight(node.getHeight());
			node = s; // ?
		}
		// Rebalancing Process


		return -2;
	}

	/**
	 * public String min()
	 * <p>
	 * Returns the info of the item with the smallest key in the tree,
	 * or null if the tree is empty
	 */
	//from BST page 19
	public String min() {
		return min(root).getValue();
	}

	//returns the min node at the subtree of x
	public IAVLNode min(IAVLNode x) {
		if (x.getHeight() == -1) {
			return null;
		}          //return null if the tree is empty (the root is external leaf)
		IAVLNode node = x;
		while (node.getLeft().getHeight() != -1) {
			node = node.getLeft();
		}
		return node;
	}

	/**
	 * public String max()
	 * <p>
	 * Returns the info of the item with the largest key in the tree,
	 * or null if the tree is empty
	 */
	//same as min() but with right instead of left
	public String max() {
		IAVLNode node = root;
		if (node.getHeight() == -1) {
			return null;
		}
		while (node.getRight().getHeight() != -1) {
			node = node.getRight();
		}
		return node.getValue();
	}


	/**
	 * public int[] keysToArray()
	 * <p>
	 * Returns a sorted array which contains all keys in the tree,
	 * or an empty array if the tree is empty.
	 */
	//n calls for successor - O(n) in total
	public int[] keysToArray() {
		if (empty()) {
			return new int[0];
		}    //return empty array if the tree is empty
		IAVLNode node = min(root);
		int[] arr = new int[this.size()];

		arr[0] = node.getKey();

		for (int i = 1; i < arr.length; i++) {
			arr[i] = successor(node).getKey();
			node = successor(node);
		}
		return arr;
	}

	/**
	 * public String[] infoToArray()
	 * <p>
	 * Returns an array which contains all info in the tree,
	 * sorted by their respective keys,
	 * or an empty array if the tree is empty.
	 */
	// same as keystoArray but with values
	public String[] infoToArray() {
		if (empty()) {
			return new String[0];
		}   //return empty array if the tree is empty
		IAVLNode node = min(root);
		String[] arr = new String[size()];
		arr[0] = node.getValue();
		for (int i = 1; i < arr.length; i++) {
			arr[i] = successor(node).getValue();
			node = successor(node);
		}
		return arr;
	}

	/**
	 * public int size()
	 * <p>
	 * Returns the number of nodes in the tree.
	 * <p>
	 * precondition: none
	 * postcondition: none
	 */
	//recursive size function- from WAVL page 75
	public int size() {
		int curr_size;
		IAVLNode node = root;
		if (empty()) {
			return 0;
		}  //return 0 if the tree is empty
		curr_size = this.sizeRec(node);
		return curr_size;
	}

	public int sizeRec(IAVLNode node) {
		if (node.getHeight() == -1) {
			return 0;
		}     //if the node is external leaf
		return 1 + this.sizeRec(node.getLeft()) + this.sizeRec(node.getRight());
	}

	/**
	 * public int getRoot()
	 * <p>
	 * Returns the root AVL node, or null if the tree is empty
	 * <p>
	 * precondition: none
	 * postcondition: none
	 */
	public IAVLNode getRoot() {
		if (empty()) {
			return null;
		}
		return this.root;
	}

	/**
	 * public string split(int x)
	 * <p>
	 * splits the tree into 2 trees according to the key x.
	 * Returns an array [t1, t2] with two AVL trees. keys(t1) < x < keys(t2).
	 * precondition: search(x) != null (i.e. you can also assume that the tree is not empty)
	 * postcondition: none
	 */
	public AVLTree_old[] split(int x) {
		return null;
	}

	/**
	 * public join(IAVLNode x, AVLTree t)
	 * <p>
	 * joins t and x with the tree.
	 * Returns the complexity of the operation (|tree.rank - t.rank| + 1).
	 * precondition: keys(x,t) < keys() or keys(x,t) > keys(). t/tree might be empty (rank = -1).
	 * postcondition: none
	 */
	public int join(IAVLNode x, AVLTree_old t) {
		if (empty() && t.empty()) {//if both trees are empty
			root = x;
			return 1;
		}
		if (empty()) {//if my tree is empty
			root = t.getRoot();
			insert(x.getKey(), x.getValue());
			return t.getRoot().getHeight() + 1;
		}
		if (t.empty()) {//if t is empty
			insert(x.getKey(), x.getValue());
			return root.getHeight() + 1;
		}
		if (root.getHeight() == t.getRoot().getHeight()) {//if trees height is equal
			x.setLeft(root);
			root.setParent(x);
			x.setRight(t.getRoot());
			t.getRoot().setParent(x);
			root = x;
			if (root.getHeight() == root.getLeft().getHeight()) {
				root.setHeight(root.getHeight() + 1);
			}//increase x's height if needed
		}
		if (root.getHeight() < t.getRoot().getHeight()) {//if my tree is shorter that t
			IAVLNode b = t.getRoot();
			while (b.getHeight() > x.getHeight()) {
				b = b.getLeft();
			}
			x.setParent(b.getParent());//connect the trees with x- from WAVL page 69
			x.getParent().setLeft(x);
			b.setParent(x);
			x.setRight(b);
			x.setLeft(root);
			root.setParent(x);
			x.setHeight(x.getHeight() + 1);//increase x's height
			//add rebalancing
		} else {//if my tree is higher than t
			IAVLNode b = root;
			while (b.getHeight() > x.getHeight()) {
				b = b.getLeft();
			}
			x.setParent(b.getParent());//connect the trees with x- from WAVL page 69
			x.getParent().setLeft(x);
			b.setParent(x);
			x.setRight(b);
			x.setLeft(t.getRoot());
			t.getRoot().setParent(x);
			x.setHeight(x.getHeight() + 1);//increase x's height
			//add rebalancing
		}

		return 0;
	}

	// function to create an external leaf

	//successor func for keysToArray and InfoToArray- from BST page 22
	public IAVLNode successor(IAVLNode x) {
		if (x.getRight() != null) {
			return min(x.getRight());
		}
		IAVLNode y = x.getParent();
		while (y.getHeight() != -1 && x == y.getRight()) {
			x = y;
			y = x.getParent();
		}
		return y;
	}

	//return the position we need to insert the node in the tree- from BST page 17
	public IAVLNode TreePosition(IAVLNode x, int key) {

		if (x.getHeight() == -1) {
			return null;
		}//if the tree is empty return null
		IAVLNode y = null;
		while (x.getHeight() != -1) {
			y = x;
			if (key == x.getKey()) {
				return x;
			} else if (key < x.getKey()) {
				x = x.getLeft();
			} else {
				x = x.getRight();
			}
		}
		return y;
	}

	//return true is the node is a left son
	public static boolean isLeftSon(IAVLNode node) {
		if (node.getKey() < node.getParent().getKey()) {
			return true;
		}
		return false;
	}

	/**
	 * public interface IAVLNode
	 * ! Do not delete or modify this - otherwise all tests will fail !
	 */
	public interface IAVLNode {
		public int getKey(); //returns node's key (for virtuval node return -1)

		public String getValue(); //returns node's value [info] (for virtuval node return null)

		public void setLeft(IAVLNode node); //sets left child

		public IAVLNode getLeft(); //returns left child (if there is no left child return null)

		public void setRight(IAVLNode node); //sets right child

		public IAVLNode getRight(); //returns right child (if there is no right child return null)

		public void setParent(IAVLNode node); //sets parent

		public IAVLNode getParent(); //returns the parent (if there is no parent return null)

		public boolean isRealNode(); // Returns True if this is a non-virtual AVL node

		public void setHeight(int height); // sets the height of the node

		public int getHeight(); // Returns the height of the node (-1 for virtual nodes)

	}
	public class ExtLeaf implements IAVLNode{
		private int key;
		private String info;
		private final int height=-1;
		private IAVLNode left = null;
		private IAVLNode right = null;
		private IAVLNode parent = null;
		public int getKey(){return -1;}
		public String getValue(){return "";}
		public void setLeft(IAVLNode node){};

		public IAVLNode getLeft(){return null;}
		public void setRight(IAVLNode node){}
		public IAVLNode getRight(){return null;}
		public void setParent(IAVLNode node){}
		public IAVLNode getParent(){return null;}
		public boolean isRealNode(){return false;}
		public void setHeight(int height){}
		public int getHeight(){return this.height;};

	}
	/**
	 * public class AVLNode
	 * <p>
	 * If you wish to implement classes other than AVLTree
	 * (for example AVLNode), do it in this file, not in
	 * another file.
	 * This class can and must be modified.
	 * (It must implement IAVLNode)
	 */

	public class AVLNode implements IAVLNode {

		private int key;
		private String info;
		private int height;
		private IAVLNode left = null;
		private IAVLNode right = null;
		private IAVLNode parent = null;

		public AVLNode(int key, String value) {
			this.key = key;
			this.info = value;
		}

		public AVLNode(IAVLNode parent) {
			this.height = -1;
			this.parent = parent;
		}

		public int getKey() {
			return key;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public String getValue() {
			return info;
		}

		public void setValue(String value) {
			this.info = value;
		}

		public void setLeft(IAVLNode node) {
			this.left = node;
		}

		public IAVLNode getLeft() {
			return left;
		}

		public void setRight(IAVLNode node) {
			this.right = node;
		}

		public IAVLNode getRight() {
			return right;
		}

		public void setParent(IAVLNode node) {
			this.parent = node;
		}

		public IAVLNode getParent() {
			return parent;
		}

		// Returns True if this is a non-virtual AVL node
		public boolean isRealNode() {
			return true;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getHeight() {
			return height;
		}


		/**
		 * public int rank_difference(IAVLNode p, IAVLNode s)
		 * <p>
		 * return the rank difference between 2 nodes.
		 */
		public int rank_difference(IAVLNode son) {
			return this.getHeight() - son.getHeight();
		}
	}

}
 
